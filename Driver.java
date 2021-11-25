import java.util.Scanner;

public class Driver implements CommunicationInterface{
	
	private int senderGroundStation;
	private int receiverGroundStation;
	private Constellation constellation;
	private Constellation.GroundStation[] groundStation;
	private Constellation.LEOSatellite[] leoSatellite;
	private Constellation.GSOSatellite gsoSatellite;
	
	
	@Override
	public boolean sendLEOtoLEO(int present, int target) {
		boolean transfer = false;
		if(leoSatellite[present].validJumptoAnotherLEO(target)) {
			transfer = true;
			if(present != target) {
				leoSatellite[present].sendingLEO = true;
				leoSatellite[present].target = target;
				leoSatellite[present].t.start();
			}
		}
		return transfer;
	}

	@Override
	public void sendLEOtoGSO(int present, int target) {
		leoSatellite[present].target = target;
		leoSatellite[present].sendingGSO = true;
		leoSatellite[present].t.start();
	}

	@Override
	public void sendGSOtoLEO(int present, int target){
		gsoSatellite.target = target;
		gsoSatellite.t.start();
	}

	@Override
	public void sendLEOtoGroundStation(int present, int target) {
		leoSatellite[present].target = target;
		leoSatellite[present].sendingGroundStation = true;
		leoSatellite[present].t.start();
	}
	
	@Override
	public void sendGroundStationToLEO(int present){
		Constellation.GroundStation  presentGroundStation= groundStation[present];
		presentGroundStation.t.start();
	}

	@Override
	public int findLEOforGroundStation(int groundStationLocation) {
		return groundStation[groundStationLocation].findLEOforGroundStation();
	}
	
	@Override
	public void receivedMessage(int location) {
		groundStation[location].receivedMessage = true;
		groundStation[location].t.start();
	}

	@Override
	public void transferRoute(int senderGroundStation, int receiverGroundStation) {
		
		if(senderGroundStation >= 0 && senderGroundStation <=9 && receiverGroundStation >=0 && receiverGroundStation <= 9 && receiverGroundStation != senderGroundStation){
			int senderLEO = findLEOforGroundStation(senderGroundStation);
			sendGroundStationToLEO(senderGroundStation);
			try{
				groundStation[senderGroundStation].t.join();
			}catch(Exception e){

			}
			int receiverLEO = findLEOforGroundStation(receiverGroundStation);
			boolean sendLEO2LEO = sendLEOtoLEO(senderLEO, receiverLEO);
			try{
				leoSatellite[senderLEO].t.join();
			}catch(Exception e){

			}
			if(sendLEO2LEO){
				sendLEOtoGroundStation(receiverLEO, receiverGroundStation);
				try{
					leoSatellite[receiverLEO].t.join();
				}catch(Exception e){
		
				}
			}else {
				sendLEOtoGSO(senderLEO, gsoSatellite.getLocation());
				try{
					leoSatellite[senderLEO].t.join();
				}catch(Exception e){
		
				}
				sendGSOtoLEO(gsoSatellite.getLocation(), receiverLEO);
				try{
					gsoSatellite.t.join();
				}catch(Exception e){
		
				}
				sendLEOtoGroundStation(receiverLEO, receiverGroundStation);
				try{
					leoSatellite[receiverLEO].t.join();
				}catch(Exception e){
		
				}
				
			}
			receivedMessage(receiverGroundStation);
		}else{
			System.out.println(Thread.currentThread().getName() + ": Communication not possible.");
		}
		
	}
	
	public static void main(String[] args) {
		Thread.currentThread().setName("Driver Thread");
		Driver driver = new Driver();
		
		driver.constellation = new Constellation();
		
		driver.groundStation = new Constellation.GroundStation[10];
		for(int i = 0; i<10; i++) {
			driver.groundStation[i] = driver.constellation.new GroundStation(i);
		}
		
		driver.leoSatellite = new Constellation.LEOSatellite[5];
		for(int i = 0; i<5; i++) {
			driver.leoSatellite[i] = driver.constellation.new LEOSatellite(i);
		}
		
		driver.gsoSatellite = driver.constellation.new GSOSatellite(0);
		
		
		Scanner sc = new Scanner(System.in);
		driver.senderGroundStation = sc.nextInt();
		driver.receiverGroundStation = sc.nextInt();
		
		driver.transferRoute(driver.senderGroundStation, driver.receiverGroundStation);
		sc.close();
	}

}
