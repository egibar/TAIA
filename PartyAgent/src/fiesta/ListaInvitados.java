package fiesta;
import jade.core.AID;
import jade.util.leap.ArrayList;

public class ListaInvitados{

	private static final ArrayList Lista = new ArrayList();
	
	
	public static int numInvitados(){
		return Lista.size();
	}
	
	public static void registrar(AID guest){
		Lista.add(guest);
	}
	
	public static void borrar(AID guest){
		Lista.remove(guest);
	}
	
	public static AID getGuest(int index){
		return (AID) Lista.get(index);
	}

}
