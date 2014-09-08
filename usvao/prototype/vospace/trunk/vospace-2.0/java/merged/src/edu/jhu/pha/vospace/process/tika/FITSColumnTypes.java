package edu.jhu.pha.vospace.process.tika;


public class FITSColumnTypes {

	public static String getSimpleTypeA(String type) {
        String s = type;
        
		s = s.trim();
        char c = s.charAt(0);
        s = s.substring(1);
        if (s.indexOf('.') > 0) {
            s = s.substring(0, s.indexOf('.'));
        }
        int length = Integer.parseInt(s);

        String simpleType = null;
        switch (c) {
            case 'A':
            	simpleType = "A"+length;
                break;
            case 'I':
                if (length > 10) {
                	simpleType = "J";
                } else {
                	simpleType = "K";
                }
                break;
            case 'F':
            case 'E':
            	simpleType = "E";
                break;
            case 'D':
            	simpleType = "D";
                break;
        }
        
        return simpleType;
	}
	
	public static String getSimpleTypeB(String type) {
		String simpleType = null;
		int length = getTFORMLength(type);
		char c = getTFORMType(type);
		if (length == 1 || c=='A') {
			//System.out.println(c);
			switch (c) {
				case 'P':
				case 'Q':
				case 'A':
					if (c=='A')
						simpleType = "A" + length;
					else 
						simpleType = "A";
					
					break;

				default:
					simpleType = String.valueOf(c);
			}
		}
		else {
			simpleType = "A";
		}

		return simpleType;	
	}
	

    /** Get the type in the TFORM field */
    static char getTFORMType(String tform) {

        for (int i = 0; i < tform.length(); i += 1) {
            if (!Character.isDigit(tform.charAt(i))) {
                return tform.charAt(i);
            }
        }
        return 0;
    }

    /** Get the type in a varying length column TFORM */
    static char getTFORMVarType(String tform) {

        int ind = tform.indexOf("P");
        if (ind < 0) {
            ind = tform.indexOf("Q");
        }

        if (tform.length() > ind + 1) {
            return tform.charAt(ind + 1);
        } else {
            return 0;
        }
    }

    /** Get the explicit or implied length of the TFORM field */
    static int getTFORMLength(String tform) {
        tform = tform.trim();

        if (Character.isDigit(tform.charAt(0))) {
            return initialNumber(tform);

        } else {
            return 1;
        }
    }
    
    /** Get an unsigned number at the beginning of a string */
    private static int initialNumber(String tform) {

        int i;
        for (i = 0; i < tform.length(); i += 1) {

            if (!Character.isDigit(tform.charAt(i))) {
                break;
            }

        }

        return Integer.parseInt(tform.substring(0, i));
    }
}
