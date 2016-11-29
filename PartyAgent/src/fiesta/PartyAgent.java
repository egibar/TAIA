package fiesta;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.Random;

@SuppressWarnings("serial")
public class PartyAgent extends Agent {

    private static String rol;
    private boolean comienzo= false;
    private int hambre;
    private int sed;
    private AID partyHost;



    protected void setup() {

    	
    	Random rnd = new Random();
    	
    	
    	if(this.getAID().toString().contains("Host")){
    		
    		registerAgent("Host");
    		partyHost=this.getAID();
    		System.out.println("[setup] Host esta aqui");
    		
    		addBehaviour(new LlenaBehaviour());
    		addBehaviour(new BienvenidaBehaviour());
    		addBehaviour(new DespedidaBehaviour());
    		
    	}
    	else{

    		registerAgent("Guest");
    		int wakeTime = (int) (rnd.nextDouble()* 10 + 1); //Random de 1 a 100 ( el 99 es el numero de elementeos y el 1 el primer numero
    		System.out.println(wakeTime);
    		addBehaviour(new WakerBehaviour(this, wakeTime*1000) {
    			@Override
    			protected void onWake() {
    				registerList();
    				System.out.println("[WakerBehaviour] "+myAgent.getLocalName() + ": He llegado a la fiesta");
    				hambre = (int) (rnd.nextDouble()* 4+1);
    	        	sed = (int) (rnd.nextDouble()*4+1);
    	        	System.out.println("[Control de usuario] El hambre de "+getLocalName()+" es: "+hambre);
    				addBehaviour(new SaludarBehaviour());
    				addBehaviour(new BienvenidaBehaviour());
    				System.out.println("[WakerBehaviour] "+getLocalName()+": esperando a que de comienzo la fiesta...");
    				addBehaviour(new accionBehaviour());
    			}
    			
    		});
    	}
        	
    }


	public static String getRol() {
		return rol;
	}

	private void registerList() {
		ListaInvitados.registrar(getAID());
		
	}

	private void registerAgent(String rol) {
		
		this.rol=rol;
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
	    sd.setType(rol);
	    sd.setName(this.getLocalName());
	    dfd.addServices(sd);
		
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
	}

	
	@Override
    protected void takeDown() {
		Random rnd = new Random();
		if(rnd.nextInt(100)>98){
	        System.out.println("[takeDown] "+getLocalName()+" se muere de indigestion");
	        ListaInvitados.borrar(getAID());

		}else{
			System.out.println("[takeDown] "+getLocalName()+" se marcho de la fiesta");
			ListaInvitados.borrar(getAID());

		}
    }

	private class accionBehaviour extends OneShotBehaviour{

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchContent("Disfrutad de la fiesta!"),
					 MessageTemplate.MatchSender(partyHost));

			ACLMessage mensaje = blockingReceive(mt);
			removeBehaviour(new BienvenidaBehaviour());			
			addBehaviour(new alimentarse());

		}

	}
	
	
    //HACER UNO QUE HAGA QUE TODOS ESCUCHEN AL ANFITRION CUANDO LA SALA ESTE LLENA
    //UN BEHAVIOUR PARA DEJAR LA FIESTA

	private class LlenaBehaviour extends SimpleBehaviour{

		@Override
		public void action() {
			 
			DFAgentDescription template = new DFAgentDescription();
			DFAgentDescription templateC = new DFAgentDescription();
			
	        ServiceDescription sd = new ServiceDescription();
	        ServiceDescription sdC = new ServiceDescription();
	        
	        sd.setType("Guest");
	        sdC.setType("Camarero");
	        
	        template.addServices(sd);
	        templateC.addServices(sdC);
	        
	        DFAgentDescription[] listaG;
	        DFAgentDescription[] listaC;
	        AID aux;
	        
	        try {
	          	listaG = DFService.search(myAgent, template);
	          	listaC = DFService.search(myAgent, templateC);
	          	
	          	if (ListaInvitados.numInvitados()== listaG.length){ 
	          		
	          		ACLMessage msgC= new ACLMessage(ACLMessage.REQUEST);
	          		ACLMessage msgG= new ACLMessage(ACLMessage.INFORM);
	          		
	          		for (int i=0; i<listaC.length; i++){
	          			aux=listaC[i].getName();
	          			msgC.addReceiver(aux);
	          		}  
	          		for (int i=0; i<listaG.length; i++){
	          			aux=listaG[i].getName();
	          			msgG.addReceiver(aux);
	          		} 
	          		msgC.setContent("Camareros!!!");
	          		msgG.setContent("Disfrutad de la fiesta!");
	          		send(msgC);
	          		send(msgG);
	          		System.out.println("[llenaBehaviour] Host: Que comience la fiesta");
	          		System.out.println("[llenaBehaviour] Host: Camareros!!!");
	          		comienzo=true;
	          	}
	        }
	         catch (FIPAException fe) {
	             fe.printStackTrace();
	        }
	       
			
		}

		@Override
		public boolean done() {
			return comienzo;
		}
		
	}
	
    private class SaludarBehaviour extends OneShotBehaviour {
    	
    	private AID[] Invitados;
    	
        @Override
        public void action() {
        	//registerAgent("Guest");
        	System.out.println("[OneShootBehaviour]"+myAgent.getLocalName() + " : Hola a tod@s");         

            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Host");
            template.addServices(sd);

            DFAgentDescription template2 = new DFAgentDescription();
            ServiceDescription sd2 = new ServiceDescription();
            sd2.setType("Guest");
            template2.addServices(sd2);

            try {

                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.setContent("Hola");
                msg.setSender(myAgent.getAID());
                partyHost = DFService.search(myAgent, template)[0].getName();
                msg.addReceiver(partyHost);

                for (DFAgentDescription df : DFService.search(myAgent, template2)) {
                    msg.addReceiver(df.getName());
                }

                msg.removeReceiver(myAgent.getAID());
                myAgent.send(msg);
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }

        }

    }

    private class BienvenidaBehaviour extends SimpleBehaviour {

        @Override
        public void action() {

            MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
                    								MessageTemplate.MatchContent("Hola"));
            ACLMessage msg = myAgent.receive(mt);
           
            if (msg != null && !msg.getSender().equals(myAgent.getAID())) {
                ACLMessage reply = msg.createReply();
                System.out.println("[WelcomeBehaviour] "+myAgent.getLocalName() + " : Hola " + msg.getSender().getLocalName());
                reply.setContent("Hola!!");
                myAgent.send(reply);
            }

        }

        @Override
        public boolean done() {
            return false;
        }
    }
    private class alimentarse extends SimpleBehaviour{
//    		MessageTemplate mc = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
//					 			 MessageTemplate.MatchContent("Le apetece comer algo?"));
//			MessageTemplate mb = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
//		 			 MessageTemplate.MatchContent("Le apetece beber algo?"));
		@Override
		public void action() {
	    	ACLMessage msg = myAgent.receive(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
	    	if(msg!=null){
	    		String text = msg.getContent();
	    		if(text.contains("comer"))
					addBehaviour(new Comer(msg));
	    		else if(text.contains("beber"))
	    			addBehaviour(new Beber(msg));
	    	}
//
//			MessageTemplate tp = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
//			ACLMessage msg= blockingReceive(tp);
//			if(tp.MatchContent("Le apetece comer algo?") != null){
//				addBehaviour(new Comer(msg));
//			}
//			else if(tp.MatchContent("Le apetece beber algo?")!= null){
//				addBehaviour(new Beber(msg));
//			}			
		}

		@Override
		public boolean done() {

//			if(hambre==0 && sed==0){
//				//removeBehaviour(parent);
//				removeBehaviour(new alimentarse());
//				return true;
//			}
//			else{
//				return false;
//			}
			return (hambre==0 && sed==0);
			
		}
		@Override
				public int onEnd() {
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.setContent("Adios");
					msg.addReceiver(partyHost);
					send(msg);
					System.out.println("[onEnd] "+ getLocalName()+": Adios Host");
					
					MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
										 MessageTemplate.MatchContent("Adios"));
					ACLMessage msg1 = myAgent.blockingReceive(mt);
					 
					myAgent.doDelete();
					ListaInvitados.borrar(getAID());
					return super.onEnd();
				}


    }
    private class Comer extends OneShotBehaviour{
    	   						
    	ACLMessage msg;
		
    	public Comer(ACLMessage msg) {
			this.msg=msg;
		}

		@Override
		public void action() {
			
			
			if(hambre>0){
				System.out.println("[OneShotBehaviour]"+getLocalName()+": Me comeria "+ hambre +" canapes");
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				reply.setContent("Si por favor");
				System.out.println("[OneShotBehaviour]"+getLocalName()+": Si, por favor");
				myAgent.send(reply);
				
				hambre--;
			}
			else{
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
				reply.setContent("No, gracias");
				System.out.println("[OneShotBehaviour]"+getLocalName()+": No, gracias");
				myAgent.send(reply);
			}
			
			
		}
    }
    
    private class Beber extends OneShotBehaviour{
			
		ACLMessage msg;
		
		public Beber(ACLMessage msg) {
			this.msg=msg;
		}

		@Override
		public void action() {

			if (sed > 0) {
				System.out.println("[OneShotBehaviour]"+getLocalName() + ": Me beberia " + sed + " cervezas");
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				reply.setContent("Si por favor");
				System.out.println("[OneShotBehaviour]"+getLocalName()+": Si, por favor");
				myAgent.send(reply);

				sed--;
			} else {
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
				reply.setContent("No, gracias");
				System.out.println("[OneShotBehaviour]"+getLocalName()+": No, gracias");
				myAgent.send(reply);
			}
		}
    	
    }
    
    private class DespedidaBehaviour extends SimpleBehaviour{

		@Override
		public void action() {
			 MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchContent("Adios"));
			 ACLMessage msg = myAgent.receive(mt);
			 
			 if (msg != null && !msg.getSender().equals(myAgent.getAID())) {
	                ACLMessage reply = msg.createReply();
	                System.out.println("[DespedidaBehaviour] "+myAgent.getLocalName() + " : Adios " + msg.getSender().getLocalName());
	                reply.setContent("Adios");
	                myAgent.send(reply);
	            }
			
		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return false;
		}
    	
    }
}