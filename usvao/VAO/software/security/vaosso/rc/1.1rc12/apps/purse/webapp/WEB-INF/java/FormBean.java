package nvo.security.purse;
import java.util.*;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;



import org.globus.purse.registration.databaseAccess.DatabaseOptions;
import org.globus.purse.registration.databaseAccess.DatabaseManager;

import org.globus.purse.exceptions.DatabaseAccessException;
import org.globus.purse.registration.databaseAccess.UserDataHandler;

public class FormBean {
	private String name;
	private String email;
	private String userName;
	private String password1;
	private String password2;
	private String inst;
	private String phone;
	private String country;
	private Hashtable errors;
        InputStream rstrm = null;


	public boolean validate() {
		boolean allOk=true;
		if (name.equals("")) {
			errors.put("name","Please enter your full name");
			name="";
			allOk=false;
		}
//		if (inst.equals("")) {
//			errors.put("inst","Please enter your Institution");
//			inst="";
//			allOk=false;
//		}
		if (phone.equals("")) {
			errors.put("phone","Please enter your phone number");
			phone="";
			allOk=false;
		}
		if (country.equals("")) {
			errors.put("country","Please select the country");
			country="";
			allOk=false;
		}
		if (email.equals("") || (email.indexOf('@') == -1)) {
			errors.put("email","Please enter a valid email address");
			email="";
			allOk=false;
		}
		if (userName.equals("")) {
			errors.put("userName","Please enter a username");
			userName="";
			allOk=false;
		} else {
                // Validate username
                   try {
		      // Find properties file for the db
		      Properties prop = new Properties();
		      rstrm=this.getClass().getResourceAsStream ("db.properties");
                      if (rstrm != null) prop.load(rstrm);

		      DatabaseOptions dbOptions =
                	   new DatabaseOptions(prop.getProperty("dbDriver"),
                                    prop.getProperty("dbConnectionURL"),
                                    prop.getProperty("dbUsername"),
                                    prop.getProperty("dbPassword"),
                                    prop.getProperty("dbPropFile"),
                                    Integer.parseInt(prop.getProperty("hashIterations")));
                                    DatabaseManager.initialize(dbOptions);

                       if (UserDataHandler.userNameExists(userName)) {
			 String msg = "Duplicate User: Try a different username";
                          errors.put("userName",msg);
			  allOk=false;
                       }
                   } catch (Exception exp) {
			  errors.put("userName","Can't access database:" +exp);
                   }
		}


		if (password1.equals("") ) {
			errors.put("password1","Please enter a valid password");
			password1="";
			allOk=false;
		}
		if (!password1.equals("") && (password2.equals("") || !password1.equals(password2))) {
			errors.put("password2","Please confirm your password");
			password2="";
			allOk=false;
		}
		return allOk;
	}

	public String getErrorMsg(String s) {
		String errorMsg =(String)errors.get(s.trim());
	      return (errorMsg == null) ? "":errorMsg;
	}

	public FormBean() {
	 name="";
	 inst="";
	 phone="";
	 country="";
	 email="";
       userName="";
	 password1="";
	 password2="";
 	 errors = new Hashtable();
	}
	
	public String getName() {
		return name;
	}

	public String getInst() {
		return inst;
	}

	public String getPhone() {
		return phone;
	}

	public String getCountry() {
		return country;
	}

	public String getEmail() {
		return email;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword1() {
		return password1;
	}
	
	public String getPassword2() {
		return password2;
	}


	

	public void setName(String fname) {
		name =fname;
	}

	public void setInst(String ins) {
		inst=ins;
	}

	public void setPhone(String pho) {
		phone =pho;
	}

	public void setCountry(String cntry) {
		country =cntry;
	}

	public void setEmail(String eml) {
		email=eml;
	}

	public void setUserName(String u) {
		userName=u;
	}

	public void  setPassword1(String p1) {
		password1=p1;
	}

	public void  setPassword2(String p2) {
		password2=p2;
	}

	public void setErrors(String key, String msg) {	
		errors.put(key,msg);
	}

}



