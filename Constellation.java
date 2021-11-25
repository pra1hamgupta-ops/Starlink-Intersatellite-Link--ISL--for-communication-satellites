
public class Constellation{
	
	public class Satellite{
		
		protected int presentLocation; 
		
		public Satellite(int location) {
			presentLocation = location; 
		}
	}
	
	public class LEOSatellite extends Satellite implements Runnable{
		Thread t;
		int target;
		boolean sendingLEO;
		boolean sendingGroundStation;
		boolean sendingGSO;
		
		
		public LEOSatellite(int location) {
			super(location);
			t = new Thread(this, "LEOSatellite " + location + " thread"); 
		}
		
		public void run(){
			if(sendingLEO) {
				System.out.println(Thread.currentThread().getName() + ": This is LEOSatellite " + presentLocation + ". Sending message to LEOSatellite " + target + ".");
				sendingLEO = false;
			}
			
			if(sendingGroundStation) {
				System.out.println(Thread.currentThread().getName() + ": This is LEOSatellite " + presentLocation + ". Sending message to GroundStation " + target + ".");
				sendingGroundStation = false;
			}
			
			if(sendingGSO) {
				System.out.println(Thread.currentThread().getName() + ": This is LEOSatellite " + presentLocation + ". Sending message to GSOSatellite " + target + ".");
				sendingGSO = false;
			}
			
			
		}
		
		
		
		public boolean validJumptoAnotherLEO(int targetLEO) {
			if(this.presentLocation == targetLEO) return  true;
			if(this.presentLocation == 0 ) {
				if(targetLEO == 1) return  true;
				else return false;
			}
			else if(this.presentLocation == 4) {
				if(targetLEO == 3) return  true;
				else return false;
			}
			else {
				if(targetLEO == this.presentLocation-1 || targetLEO == this.presentLocation +1) return true;
				else return false;
			}
		}
	}
	
	public class GSOSatellite extends Satellite implements Runnable{
		Thread t;
		int target;
		
		public GSOSatellite(int location) {
			super(location);
			t = new Thread(this, "GSOSatellite " + location + " thread");
		}
		
		public int getLocation() {
			return this.presentLocation;
		}
		
		public void run() {
			System.out.println(Thread.currentThread().getName() + ":" + " This is GSOsatellite " + presentLocation + ". Sending message to LEOsatellite " + target + ".");
			
		}
		
	}
	
	public class GroundStation implements Runnable{
		Thread t;
		private int location; 
		boolean receivedMessage;
		public GroundStation(int location) {
			this.location = location;
			t = new Thread(this, "GroundStation " + location + " thread");
		}
		

		public int findLEOforGroundStation() {
			return location/2;
		}
		
		public void run() {
			if(receivedMessage) {
				System.out.println(Thread.currentThread().getName() + ": This is GroundStation " + location + ". recieved message.");
				receivedMessage = false;
			}else {
				int leo = findLEOforGroundStation();
				System.out.println(Thread.currentThread().getName() + ":" + " This is GroundStation " + location + ". Sending message to LEOsatellite " + leo + ".");
			}
		}
	}
}