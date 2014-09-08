import java.net.InetAddress;

public class DnsTest {
    public static void main(String[] args) throws Exception {
	InetAddress addrs[] = InetAddress.getAllByName("sso.us-vo.org");
	System.out.println("Found " + addrs.length + " addresses:");
	for (InetAddress addr : addrs) {
	    System.out.println("  * " + addr);
	}
    }
}