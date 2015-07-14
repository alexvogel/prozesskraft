package de.caegroup.commons;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;

import com.license4j.License;
import com.license4j.LicenseValidator;

public class MyLicense {

	/*----------------------------
	  structure
	----------------------------*/

//	private long time = System.currentTimeMillis();
	private License license = null;
	private ArrayList<String> log = new ArrayList<String>();
	private int port;
	private String host;
	private String productId = null;
	private String productEdition = null;
	private String productVersion = null;
	private InetAddress inetAddressHost;
	
	private String publicKey =	"30819f300d06092a864886f70d010101050003818d003081893032301006"
							+ "072a8648ce3d02002EC311215SHA512withECDSA106052b81040006031e0"
							+ "0046454eb11885791e7d6dc167597769bd45831b10ea92852e9901793f7G"
							+ "02818100c878fab37f201dbc1cd0c9519a83d9457838838f1e930ba1cfd4"
							+ "afb853692d91d1cc7c93f92bc9a6876fb7f814ebc784d1a08650d4562ad6"
							+ "1aabaf8ddce439790bad2b60f76ea67766112a0ba0a6e962abdb4543692a"
							+ "a2c75f055b8d0d725d7b03RSA4102413SHA512withRSA7bde6ece2cb63b4"
							+ "415b1b9ca46b2e87b48a876254dd24db0d005298b3e9870d10203010001";

	/*----------------------------
	  constructors
	----------------------------*/
	public MyLicense(ArrayList<String> allPortAtHost, String productId, String productEdition, String productVersion)
	{
		this.productId = productId;
		this.productEdition = productEdition;
		this.productVersion = productVersion;

		boolean validLicenseFound = false;
		
		for(String actPortAtHost : allPortAtHost)
		{
			
			if(validLicenseFound == true)
			{
				break;
			}
			
			else
			{
				// falls null, dann ueberspringen
				if(!(actPortAtHost == null))
				{
					String[] port_and_host = actPortAtHost.split("@");
		
					this.port = Integer.parseInt(port_and_host[0]);
					this.host = port_and_host[1];
	
					try {
						this.inetAddressHost = InetAddress.getByName(host);
					} catch (UnknownHostException e) {
						log.add("["+new Timestamp(System.currentTimeMillis()) + "]:"+"warn:"+"unknown host "+host);
						e.printStackTrace();
					}
		
					log.add("["+new Timestamp(System.currentTimeMillis()) + "]:"+"info:"+"trying license-server "+port+"@"+host);
		
					try
					{
						this.license = LicenseValidator.validate(publicKey, productId, productEdition, productVersion, null, null, inetAddressHost, port, null, null, null);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
		
//					log.add("["+new Timestamp(System.currentTimeMillis()) + "]:"+"debug:"+"port@host      : "+port+"@"+host);
//					log.add("["+new Timestamp(System.currentTimeMillis()) + "]:"+"debug:"+"product-id     : "+productId);
//					log.add("["+new Timestamp(System.currentTimeMillis()) + "]:"+"debug:"+"product-edition: "+productEdition);
//					log.add("["+new Timestamp(System.currentTimeMillis()) + "]:"+"debug:"+"product-version: "+productVersion);
//					
//					System.err.println("debug: license validation status is: " + license.getValidationStatus());
					
					switch(license.getValidationStatus())
					{
						case LICENSE_VALID:
							log.add("["+new Timestamp(System.currentTimeMillis()) + "]:"+"info:"+"license validation returns "+license.getValidationStatus().toString());
							log.add("["+new Timestamp(System.currentTimeMillis()) + "]:"+"info:"+"license issued for "+license.getLicenseText().getUserEMail()+ " expires in "+license.getLicenseText().getLicenseExpireDaysRemaining(null)+" day(s).");
							validLicenseFound = true;
							break;
						case LICENSE_INVALID:
							log.add("["+new Timestamp(System.currentTimeMillis()) + "]:"+"info:"+"license validation returns "+license.getValidationStatus().toString());
							validLicenseFound = false;
							break;
						default:
							log.add("["+new Timestamp(System.currentTimeMillis()) + "]:"+"fatal:"+"no valid license found at this license-server.");
					}
				}
			}
		}
		
		if(validLicenseFound == false)
		{
			log.add("["+new Timestamp(System.currentTimeMillis()) + "]:"+"fatal:"+"no valid license found at all.");
		}
		
	}

	/*----------------------------
	  methods
	----------------------------*/
	public boolean isValid()
	{
		boolean valid = false;
		
		try
		{
			switch(license.getValidationStatus())
			{
				case LICENSE_VALID:
					valid = true;
					break;
				default:
					valid = false;
			}
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();;
		}
		return valid;
	}
	
	/*----------------------------
	  methods getter & setter
	----------------------------*/
	public ArrayList<String> getLog()
	{
		return this.log;
	}

}
