import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GUI implements ActionListener {
    private JFrame frame = new JFrame();
    JTextField sender;
    JTextField receiver;
	static StringBuilder response;
	static GUI gui;
	JLabel output;
	Container container;

    public GUI() {

        JLabel senderGroundStation = new JLabel("Enter Sender GroundStation : ");
        sender = new JTextField("0");
        JLabel receiverGroundStation = new JLabel("Enter Receiver GroundStation");
        receiver = new JTextField("0");

        
        JButton button = new JButton("Send");
        button.addActionListener(this);
        

		container = frame.getContentPane();

		BoxLayout boxLayout = new BoxLayout(container, BoxLayout.Y_AXIS);
		container.setLayout(boxLayout);
		container.add(senderGroundStation);
        container.add(sender);
        container.add(receiverGroundStation);
        container.add(receiver);
        container.add(button);
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setBounds(100,100,500,400);
        frame.setTitle("Starlink Intersatellite Link (ISL) for communication satellites");
        frame.pack();
        frame.setVisible(true);
    }

    
    public void actionPerformed(ActionEvent e) {
        String[] query = new String[2];
        query[0] = sender.getText();
        query[1] = receiver.getText();
		response = new StringBuilder();

		String s = "\n\nSending message from GroundStation " + query[0] + " to GroundStation " + query[1] + "->\n";
		response.append(s);
        Driver.main(query);
		response.append("\n\n");
		output = new JLabel();
		output.setText("<html>" + response.toString().replaceAll("<","&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br/>") + "</html>");
		container.add(output);
    }

   
    public static void main(String[] args) {
        gui = new GUI();
    }

	interface CommunicationInterface{
		public boolean sendLEOtoLEO(int present, int target);
		public void sendLEOtoGSO(int present, int target);
		public void sendGSOtoLEO(int present, int target);
		public void sendLEOtoGroundStation(int present, int target);
		public void sendGroundStationToLEO(int present);
		public int findLEOforGroundStation(int groundStationLocation);
		public void transferRoute(int senderGroundStation, int receiverGroundStation);
		public void receivedMessage(int location);
	}
	
	
	class Driver implements CommunicationInterface{
		
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
				try{
					groundStation[receiverGroundStation].t.join();
				}catch(Exception e){
		
				}
			}else{
				response.append(Thread.currentThread().getName() + ": Communication not possible.\n");
			}
			
		}
		
		public static void main(String[] args) {
			Thread.currentThread().setName("Driver Thread");
			Driver driver = gui.new Driver();
			
			driver.constellation = gui.new Constellation();
			
			driver.groundStation = new Constellation.GroundStation[10];
			for(int i = 0; i<10; i++) {
				driver.groundStation[i] = driver.constellation.new GroundStation(i);
			}
			
			driver.leoSatellite = new Constellation.LEOSatellite[5];
			for(int i = 0; i<5; i++) {
				driver.leoSatellite[i] = driver.constellation.new LEOSatellite(i);
			}
			
			driver.gsoSatellite = driver.constellation.new GSOSatellite(0);
			
			
			
			driver.senderGroundStation = Integer.parseInt(args[0]);
			driver.receiverGroundStation = Integer.parseInt(args[1]);
			
			driver.transferRoute(driver.senderGroundStation, driver.receiverGroundStation);
		}
	
	}
	
	
	class Constellation{
		
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
					response.append(Thread.currentThread().getName() + ": This is LEOSatellite " + presentLocation + ". Sending message to LEOSatellite " + target + ".\n");
					sendingLEO = false;
				}
				
				if(sendingGroundStation) {
					response.append(Thread.currentThread().getName() + ": This is LEOSatellite " + presentLocation + ". Sending message to GroundStation " + target + ".\n");
					
					sendingGroundStation = false;
				}
				
				if(sendingGSO) {
					response.append(Thread.currentThread().getName() + ": This is LEOSatellite " + presentLocation + ". Sending message to GSOSatellite " + target + ".\n");
					sendingGSO = false;
				}
				
				
			}
			
			
			// I am assuming that LEO 0 can't go to LEO 4 in one hop
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
				response.append(Thread.currentThread().getName() + ":" + " This is GSOsatellite " + presentLocation + ". Sending message to LEOsatellite " + target + ".\n");
				
			}
			
		}
		
		public class GroundStation implements Runnable{
			Thread t;
			Driver d;
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
					response.append(Thread.currentThread().getName() + ": This is GroundStation " + location + ". recieved message.\n");
					receivedMessage = false;
				}else {
					int leo = findLEOforGroundStation();
					response.append(Thread.currentThread().getName() + ":" + " This is GroundStation " + location + ". Sending message to LEOsatellite " + leo + ".\n");
				}
			}
		}
	}
	
}


