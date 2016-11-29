package fiesta;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Random;

@SuppressWarnings("serial")
public class Camarero extends Agent {

	protected void setup() {
		registerCamarero(getLocalName());
		Random rnd = new Random();
		ACLMessage msg = this.receive();
		System.out.println("[setup] Agente " + getLocalName() + " : esperando la llamada de trabajo");
		ACLMessage mensaje = blockingReceive(MessageTemplate.MatchPerformative(ACLMessage.REQUEST));
		System.out.println("[setup]Agente " + getLocalName() + " : es hora de trabajar");
		ACLMessage reply = new ACLMessage(ACLMessage.INFORM);
		reply.setContent("a sus ordenes");
		System.out.println("[setup] Agente "+getLocalName()+" : "+reply.getContent());
		send(reply);

		addBehaviour(new TickerBehaviour(this, 3000) {
			@Override
			protected void onTick() {
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				ServiceDescription sd1 = new ServiceDescription();
				sd.setType("Guest");
				sd1.setType("Host");
				template.addServices(sd);

				DFAgentDescription[] lista;
				AID aux;

				try {

					lista = DFService.search(myAgent, template);

					if(ListaInvitados.numInvitados()==0/*listacomida.length==0 && listabebida.length==0*/){
						System.out.println("[Tickerbehaviour] " + getLocalName()+": Se acabo la fiesta, hora de dormir");
						//ELIMINAR EL COMPORTAMIENTO
						//myAgent.removeBehaviour(this);
						myAgent.doDelete();
					}
					else{
						Random random = new Random();
						int invitado = (int) (random.nextDouble()* ListaInvitados.numInvitados());
						
						String[] content={"comer","beber"};
						int eleccion = (int) (random.nextDouble()*2);
						
						ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
						msg.setSender(myAgent.getAID());
						
						msg.addReceiver(ListaInvitados.getGuest(invitado));
						
						msg.setContent("Le apetece "+ content[eleccion]+" algo?");
						String conver= content[eleccion] + ListaInvitados.getGuest(invitado);
						msg.setConversationId(conver);
						System.out.println("[Tickerbehaviour] " + getLocalName()+" : "+ListaInvitados.getGuest(invitado).getLocalName()+" Le apetece "+ content[eleccion]+ " algo?");
						myAgent.send(msg);
						MessageTemplate mt = MessageTemplate.MatchConversationId(conver);

						ACLMessage msg2 = myAgent.blockingReceive(mt);

						if (msg2 != null && msg2.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
							ACLMessage reply = msg2.createReply();
							reply.setPerformative(ACLMessage.CONFIRM);
							System.out.println("[Tickerbehaviour] "+getLocalName() + " : Aqui tienes "
									+ msg2.getSender().getLocalName());
							myAgent.send(reply);
						}
						else if(msg2 != null && msg2.getPerformative() == ACLMessage.REJECT_PROPOSAL){
							ACLMessage reply = msg2.createReply();
							reply.setPerformative(ACLMessage.CONFIRM);
							System.out.println("[Tickerbehaviour] "+getLocalName() + " : No hay de que "
									+ msg2.getSender().getLocalName());
							myAgent.send(reply);
						}

					}
				} catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}

		});
	}

	private void registerCamarero(String localName) {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Camarero");
		sd.setName(this.getLocalName());
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

	}

	@Override
	protected void takeDown() {
		System.out.println("[takeDown] "+getLocalName() + " se marcha de la fiesta");
	}

}
