
public interface CommunicationInterface{
	public boolean sendLEOtoLEO(int present, int target);
	public void sendLEOtoGSO(int present, int target);
	public void sendGSOtoLEO(int present, int target);
	public void sendLEOtoGroundStation(int present, int target);
	public void sendGroundStationToLEO(int present);
	public int findLEOforGroundStation(int groundStationLocation);
	public void transferRoute(int senderGroundStation, int receiverGroundStation);
	public void receivedMessage(int location);
}