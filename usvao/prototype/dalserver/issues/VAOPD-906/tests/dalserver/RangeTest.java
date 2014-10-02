package dalserver;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Range Class Test Suite
 *   Constructors:
 *     The Range class constructors all fall back to the most complex flavor.
 *     By testing the various combinations of that constructor, the simpler
 *     cases are included.
 *
 *   Issues:
 *     2. ANY type forces numeric=true for non-numeric values.
 *     4. Numeric value is valid Date?? Possible DateParser issue
 *    10. Constructors without isoDate argument throw InvalidDateException
 *         with NO possiblity of being interpreted as a Date.
 *    11. Single value types, string rep != dtype rep.
 *    12. ANY type, generates NPE accessing values as numeric.
 *    14. String comparisons do not handle mixed null on value2. (HIVAL w/ CLOSED)
 *
 */
public class RangeTest {

    Range r = null;

    // Expected results
    String  exp_constructor;
    boolean exp_numeric;
    boolean exp_isdate;
    String  exp_value1;
    String  exp_value2;

    // Test values
    String[] nullvals = {        null,        null};
    String[] datevals = {"1999-08-29","2013-08-29"};
    String[] numvals  = {        "10",       "100"};
    String[] strvals  = {     "alpha",     "omega"};

    int testnum;
    boolean verbose = true;

    @Before 
    public void setup(){
    }

    @After 
    public void teardown() {
    }

    /**
     *  Range Constructor Tests:
     * 
     * test           type   v1    v2   order   date
     *  
     *      Range( ONEVAL,     *,  null,  true,   *   ) - Not done, order param irrelevant
     *      Range(  LOVAL,     *,  null,  true,   *   ) - Not done, order param irrelevant
     *      Range(  HIVAL,     *,  null,  true,   *   ) - Not done, order param irrelevant
     *
     *      Range( CLOSED,  dat1,  dat2, false, false ) - Not done
     *      Range( CLOSED,  dat2,  dat1,  true, false ) - Not done
     *      Range( CLOSED,  num1,  num2, false,  true ) - Not done
     *      Range( CLOSED,  str1,  str2, false,  true ) - Not done
     *      Range( CLOSED,  num2,  num1,  true,  true ) - Not done
     *      Range( CLOSED,  str2,  str1,  true,  true ) - Not done
     *      Range( CLOSED,  str1,  num2, false, false ) - Not done
     *      Range( CLOSED,  str1,  dat2, false, false ) - Not done
     *      Range( CLOSED,  num1,  str2, false, false ) - Not done
     */

    @Test 
    /**
     * test Constructors - IAE
     *   Consolidates test cases for invalid argument combinations.
     *   Each of these are expected to throw an InvalidArgumentException
     *   due to value arguements being inconsistent with range type.
     *
     *      Range(    ANY,  null, !null,   n/a,  n/a  ) = IAE
     *      Range(    ANY, !null,  null,   n/a,  n/a  ) = IAE
     *      Range(    ANY, !null, !null,   n/a,  n/a  ) = IAE
     *       
     *      Range( ONEVAL,  null,  null,   n/a,  n/a  ) = IAE
     *      Range(  LOVAL,  null,  null,   n/a,  n/a  ) = IAE
     *      Range(  HIVAL,  null,  null,   n/a,  n/a  ) = IAE
     *      
     *      Range( ONEVAL,  null, !null,   n/a,  n/a  ) = IAE
     *      Range(  LOVAL,  null, !null,   n/a,  n/a  ) = IAE
     *      Range(  HIVAL,  null, !null,   n/a,  n/a  ) = IAE
     *      
     *      Range( ONEVAL, !null, !null,   n/a,  n/a  ) = IAE
     *      Range(  LOVAL, !null, !null,   n/a,  n/a  ) = IAE
     *      Range(  HIVAL, !null, !null,   n/a,  n/a  ) = IAE
     *      
     *      Range( CLOSED,  null,  null,   n/a,  n/a  ) = IAE
     *      Range( CLOSED,  null, !null,   n/a,  n/a  ) = IAE
     *      Range( CLOSED, !null,  null,   n/a,  n/a  ) = IAE
     *       
     */
    public void testIAE()
    {
       if ( verbose )
         System.out.println("Test Constructors and IAE ");

       testnum = -1;

       // Set Result Expectations
       exp_constructor = "IAE";
       exp_numeric = false;    // irrelevant
       exp_isdate  = false;    // irrelevant
       exp_value1  = null;     // irrelevant
       exp_value2  = null;     // irrelevant

       // execute test
       // ANY type requires 0 values
       baseTest( RangeType.ANY, nullvals[0], numvals[1], false, false );
       baseTest( RangeType.ANY,  strvals[0], nullvals[1], false, false );
       baseTest( RangeType.ANY,  numvals[0], numvals[1], false, false );

       // Single types require 1 value, must be on value1.
       baseTest( RangeType.ONEVAL, nullvals[0], nullvals[1], false, false );
       baseTest( RangeType.LOVAL,  nullvals[0], nullvals[1], false, false );
       baseTest( RangeType.HIVAL,  nullvals[0], nullvals[1], false, false );

       baseTest( RangeType.ONEVAL, nullvals[0],  numvals[1], false, false );
       baseTest( RangeType.LOVAL,  nullvals[0], datevals[1], false, false );
       baseTest( RangeType.HIVAL,  nullvals[0],  strvals[1], false, false );

       baseTest( RangeType.ONEVAL, datevals[0], datevals[1], false, true );
       baseTest( RangeType.LOVAL,   strvals[0],  strvals[1], false, false );
       baseTest( RangeType.HIVAL,   numvals[0],  numvals[1], false, false );

       // Closed type requres 2 values
       baseTest( RangeType.CLOSED, nullvals[0], nullvals[1], false, false );
       baseTest( RangeType.CLOSED, datevals[0], nullvals[1], false, false );
       baseTest( RangeType.CLOSED, nullvals[0],  strvals[1], false, false );


       if ( verbose )
         System.out.println("Test Constructors and IAE = Done.");
    }

    @Test 
    /**
     * Constructor Test:
     *      Range(    ANY,  null,  null, false, false )
     */
    public void test1()
    {
       testnum = 1;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.ANY, nullvals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     *      Range(    ANY,  null,  null,  true, false )
     */
    public void test2()
    {
       testnum = 2;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.ANY, nullvals[0], nullvals[1], true, false );

    }

    @Test 
    /**
     * 
     *      Range(    ANY,  null,  null, false,  true )
     */
    public void test3()
    {
       testnum = 3;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = true;
       exp_isdate  = true;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.ANY, nullvals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     *      Range(    ANY,  null,  null,  true,  true )
     */
    public void test4()
    {
       testnum = 4;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = true;
       exp_isdate  = true;
       exp_value1  = null;
       exp_value2  = null;

       // execute test
       baseTest( RangeType.ANY, nullvals[0], nullvals[1], true, true );

    }

    @Test 
    /**
     * 
     *      Range(  LOVAL,  num1,  null, false, false )
     */
    public void test5()
    {
       testnum = 5;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = numvals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.LOVAL, numvals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     *      Range(  HIVAL,  num1,  null, false, false )
     */
    public void test6()
    {
       testnum = 6;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = numvals[0]; // Single value type populates value1 
       exp_value2  = null;

       // execute test
       baseTest( RangeType.HIVAL, numvals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     *      Range( ONEVAL,  num1,  null, false, false )
     */
    public void test7()
    {
       testnum = 7;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = numvals[0]; // Single value type populates value1 
       exp_value2  = null;       // extra

       // execute test
       baseTest( RangeType.ONEVAL, numvals[0], nullvals[1], false, false );

    }


    @Test 
    /**
     * 
     *      Range(  LOVAL,  num1,  null, false,  true )
     */
    public void test8()
    {
       testnum = 8;

       // Set Result Expectations
       exp_constructor = "IDE";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = numvals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.LOVAL, numvals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     *      Range(  HIVAL,  num1,  null, false,  true )
     */
    public void test9()
    {
       testnum = 9;

       // Set Result Expectations
       exp_constructor = "IDE";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = numvals[0]; // Single value type populates value1 
       exp_value2  = null;

       // execute test
       baseTest( RangeType.HIVAL, numvals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     *      Range( ONEVAL,  num1,  null, false,  true )
     */
    public void test10()
    {
       testnum = 10;

       // Set Result Expectations
       exp_constructor = "IDE";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = numvals[0]; // Single value type populates value1 
       exp_value2  = null;       // extra

       // execute test
       baseTest( RangeType.ONEVAL, numvals[0], nullvals[1], false, true );

    }


    @Test 
    /**
     * 
     *      Range(  LOVAL,  dat1,  null, false, false )
     */
    public void test11()
    {
       testnum = 11;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = datevals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.LOVAL, datevals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     *      Range(  HIVAL,  dat1,  null, false, false )
     */
    public void test12()
    {
       testnum = 12;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = datevals[0]; // Single value type populates value1 
       exp_value2  = null;

       // execute test
       baseTest( RangeType.HIVAL, datevals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     *      Range( ONEVAL,  dat1,  null, false, false )
     */
    public void test13()
    {
       testnum = 13;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = datevals[0]; // Single value type populates value1 
       exp_value2  = null;

       // execute test
       baseTest( RangeType.ONEVAL, datevals[0], nullvals[1], false, false );

    }


    @Test 
    /**
     * 
     *      Range(  LOVAL,  dat1,  null, false,  true )
     */
    public void test14()
    {
       testnum = 14;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = datevals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.LOVAL, datevals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     *      Range(  HIVAL,  dat1,  null, false,  true )
     */
    public void test15()
    {
       testnum = 15;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = datevals[0]; // Single value type populates value1 
       exp_value2  = null;

       // execute test
       baseTest( RangeType.HIVAL, datevals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     *      Range( ONEVAL,  dat1,  null, false,  true )
     */
    public void test16()
    {
       testnum = 16;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = datevals[0]; // Single value type populates value1 
       exp_value2  = null;

       // execute test
       baseTest( RangeType.ONEVAL, datevals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     *      Range(  LOVAL,  str1,  null, false, false )
     */
    public void test17()
    {
       testnum = 17;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = strvals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.LOVAL, strvals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     *      Range(  HIVAL,  str1,  null, false, false )
     */
    public void test18()
    {
       testnum = 18;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = strvals[0]; // Single value type populates value1 
       exp_value2  = null;

       // execute test
       baseTest( RangeType.HIVAL, strvals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     *      Range( ONEVAL,  str1,  null, false, false )
     */
    public void test19()
    {
       testnum = 19;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = strvals[0]; // Single value type populates value1 
       exp_value2  = null;

       // execute test
       baseTest( RangeType.ONEVAL, strvals[0], nullvals[1], false, false );

    }

    @Test 
    /**
     * 
     *      Range(  LOVAL,  str1,  null, false,  true )
     */
    public void test20()
    {
       testnum = 20;

       // Set Result Expectations
       exp_constructor = "IDE";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = strvals[0];
       exp_value2  = null;

       // execute test
       baseTest( RangeType.LOVAL, strvals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     *      Range(  HIVAL,  str1,  null, false,  true )
     */
    public void test21()
    {
       testnum = 21;

       // Set Result Expectations
       exp_constructor = "IDE";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = strvals[0]; // Single value type populates value1 
       exp_value2  = null;

       // execute test
       baseTest( RangeType.HIVAL, strvals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     *      Range( ONEVAL,  str1,  null, false,  true )
     */
    public void test22()
    {
       testnum = 22;

       // Set Result Expectations
       exp_constructor = "IDE";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = strvals[0]; // Single value type populates value1 
       exp_value2  = null;

       // execute test
       baseTest( RangeType.ONEVAL, strvals[0], nullvals[1], false, true );

    }

    @Test 
    /**
     * 
     *      Range( CLOSED,  num1,  num2, false, false )
     */
    public void test23()
    {
       testnum = 23;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = numvals[0];
       exp_value2  = numvals[1];

       // execute test
       baseTest( RangeType.CLOSED, numvals[0], numvals[1], false, false );

    }

    @Test 
    /**
     * 
     *      Range( CLOSED,  num2,  num1,  true, false )
     */
    public void test24()
    {
       testnum = 24;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = true;
       exp_isdate  = false;
       exp_value1  = numvals[0];
       exp_value2  = numvals[1];

       // execute test
       baseTest( RangeType.CLOSED, numvals[1], numvals[0], true, false );

    }

    @Test 
    /**
     * 
     *      Range( CLOSED,  dat1,  dat2, false,  true )
     */
    public void test25()
    {
       testnum = 25;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = datevals[0];
       exp_value2  = datevals[1];

       // execute test
       baseTest( RangeType.CLOSED, datevals[0], datevals[1], false, true );

    }


    @Test 
    /**
     * 
     *      Range( CLOSED,  dat2,  dat1,  true,  true )
     */
    public void test26()
    {
       testnum = 26;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = true;
       exp_value1  = datevals[0];
       exp_value2  = datevals[1];

       // execute test
       baseTest( RangeType.CLOSED, datevals[1], datevals[0], true, true );

    }


    @Test 
    /**
     * 
     *      Range( CLOSED,  str1,  str2, false, false )
     */
    public void test27()
    {
       testnum = 27;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = strvals[0];
       exp_value2  = strvals[1];

       // execute test
       baseTest( RangeType.CLOSED, strvals[0], strvals[1], false, false );

    }

    @Test 
    /**
     * 
     *      Range( CLOSED,  str2,  str1,  true, false )
     */
    public void test28()
    {
       testnum = 28;

       // Set Result Expectations
       exp_constructor = "OK";
       exp_numeric = false;
       exp_isdate  = false;
       exp_value1  = strvals[0];
       exp_value2  = strvals[1];

       // execute test
       baseTest( RangeType.CLOSED, strvals[1], strvals[0], true, false );

    }

    @Test 
    /**
     * 
     */
    public void testIssue10()
    {
      try{
        r = new Range( RangeType.ANY );
      }
      catch ( InvalidDateException ide ){
        fail( ide.getMessage() );
      }
      System.out.println("".format("Test Issue10 - Issue 10: Constructors require IDE catch when not possible..") );
    }


    @Test 
    /**
     * Tests non-string accessors to values
     */
    public void testConversionAccess()
    {
       Range a = null;
       Range b = null;
       Range c = null;
       Range d = null;
       Range e = null;
       Range f = null;
       Range g = null;
       Range h = null;

       int    ival;
       double dval;

       //Create test ranges..
       try {
          //  - single value type, with numeric, string, date values
          a = new Range( RangeType.LOVAL,  numvals[0], nullvals[1], false, false );
          b = new Range( RangeType.LOVAL,  strvals[0], nullvals[1], false, false );
          c = new Range( RangeType.LOVAL, datevals[0], nullvals[1], false, true  );
          
          //  -    two value type, with numeric, string, date values
          d = new Range( RangeType.CLOSED,  numvals[0],  numvals[1], false, false );
          e = new Range( RangeType.CLOSED,  strvals[0],  strvals[1], false, false );
          f = new Range( RangeType.CLOSED, datevals[0], datevals[1], false, true  );

          //  - ANY type, with null values
          g = new Range( RangeType.ANY, nullvals[0], nullvals[1], false, false  );
       }
       catch ( InvalidDateException ide )
       {
         fail( ide.getMessage() );
       }


       // Access first value as Numeric
       try {
         // These should succeed.. all numeric type ranges
         assertEquals( a.intValue1(), new Integer( numvals[0]).intValue() );
         assertEquals( d.intValue1(), new Integer( numvals[0]).intValue() );

         assertEquals( a.doubleValue1(), new Double( numvals[0]).doubleValue(), 1e-7 );
         assertEquals( d.doubleValue1(), new Double( numvals[0]).doubleValue(), 1e-7 );
         
       }
       catch ( NumberFormatException nfe )
       {
         fail( nfe.getMessage() );
       }

       // These should fail.
       //   - string and date values should generate NFE
       failCheckValue1( b );
       failCheckValue1( c );
       failCheckValue1( e );
       failCheckValue1( f );

       // Access second value as Numeric
       try {
         // These should succeed.. all numeric type ranges
         assertEquals( a.intValue2(), new Integer( numvals[0]).intValue() );
         assertEquals( d.intValue2(), new Integer( numvals[1]).intValue() );

         assertEquals( a.doubleValue2(), new Double( numvals[0]).doubleValue(), 1e-7 );
         assertEquals( d.doubleValue2(), new Double( numvals[1]).doubleValue(), 1e-7 );
         
       }
       catch ( NumberFormatException nfe )
       {
         fail( nfe.getMessage() );
       }

       // These should fail.
       //   - string and date values should generate NFE
       failCheckValue2( b );
       failCheckValue2( c );
       failCheckValue2( e );
       failCheckValue2( f );

       // Access first value as Date
       assertNull( a.dateValue1() );
       assertNull( b.dateValue1() );
       assertNotNull( c.dateValue1());
       assertNull( d.dateValue1() );
       assertNull( e.dateValue1() );
       assertNotNull( f.dateValue1());

       // Access second value as Date
       assertNull( a.dateValue2() );
       assertNull( b.dateValue2() );
       assertNotNull( c.dateValue2());
       assertNull( d.dateValue2() );
       assertNull( e.dateValue2() );
       assertNotNull( f.dateValue2());

       // Issues:
       System.out.println("".format("Test ConversionAccess - Issue 11: Single value types, string rep != dtype rep.") );
       System.out.println("".format("   String  rep: value1=%s value2=%s ",a.stringValue1(),a.stringValue2() ));
       System.out.println("".format("   Integer rep: value1=%2d value2=%2d ",a.intValue1(),a.intValue2() ));

       // ANY Type - values null, getting cast.  Throws NPE
       System.out.println("".format("Test ConversionAccess - Issue 12: ANY type, generates NPE accessing values as numeric.") );
       npeCheckValue(g);

    }

    @Test 
    /**
     * Tests CompareTo method
     */
    public void testCompareTo()
    {
       Range a = null;
       Range b = null;
       Range c = null;
       Range d = null;
       Range e = null;
       Range f = null;
       Range g = null;
       Range h = null;
       Range i = null;
       //Range j = null;
       Range k = null;

       Range s1 = null;
       Range s2 = null;
       Range s3 = null;
       Range s4 = null;
       Range s5 = null;

       Range d1 = null;
       Range d2 = null;
       Range d3 = null;

       int result;

       //Create test ranges..
       try {
         a = new Range(RangeType.ONEVAL, "5");
         b = new Range(RangeType.ONEVAL, "1");
         c = new Range(RangeType.ONEVAL, "9");
         d = new Range(RangeType.CLOSED, "3", "7");
         e = new Range(RangeType.CLOSED, "7", "3");
         f = new Range(RangeType.HIVAL,  "5");
         g = new Range(RangeType.HIVAL,  "6");
         h = new Range(RangeType.ANY);
         i = new Range(RangeType.ANY, null, null, false, true );
         //j = new Range(RangeType.ONEVAL, "1", null);
         k = new Range(RangeType.CLOSED, "1", "9");
         
         s1 = new Range(RangeType.ONEVAL, "foo");
         s2 = new Range(RangeType.ONEVAL, "bar");
         s3 = new Range(RangeType.CLOSED, "aaa", "eee");
         s4 = new Range(RangeType.CLOSED, "aaa", "jjj");
         s5 = new Range(RangeType.ONEVAL, "jjj");

         d1 = new Range(RangeType.HIVAL, "1999-01-01", null, false, true );
         d2 = new Range(RangeType.LOVAL, "2003-01-01", null, false, true );
         d3 = new Range(RangeType.CLOSED,"1999-01-01", "2003-01-01", false, true );
         
       }
       catch ( InvalidDateException ide )
       {
         fail( ide.getMessage() );
       }

       // 'that' == null results in NPE
       try
       {
         result = d.compareTo(null);
         fail( "Expected exception not thrown." );
       }
       catch ( NullPointerException npe ){} // good

       // compare to itself == EQUAL
       assertEquals( 0, a.compareTo(a) );
       assertEquals( 0, s1.compareTo(s1) );

       // ANY == ANY
       assertEquals( 0, h.compareTo(i) );

       // numeric vs non-numeric == ClassCaseException
       try
       {
         result = c.compareTo(s2);
         fail( "Expected exception not thrown." );
       }
       catch ( ClassCastException cce ){} // good
       try
       {
         result = s1.compareTo(c);
         fail( "Expected exception not thrown." );
       }
       catch ( ClassCastException cce ){} // good

       // ANY vs <OTHER> type == ClassCastException
       try
       {
         result = h.compareTo(f);
         fail( "Expected exception not thrown." );
       }
       catch ( ClassCastException cce ){} // good
       try
       {
         result = f.compareTo(h);
         fail( "Expected exception not thrown." );
       }
       catch ( ClassCastException cce ){} // good
       try
       {
         result = h.compareTo(s1);
         fail( "Expected exception not thrown." );
       }
       catch ( ClassCastException cce ){} // good
       try
       {
         result = s1.compareTo(h);
         fail( "Expected exception not thrown." );
       }
       catch ( ClassCastException cce ){} // good

       // Numeric comparisons..
       assertTrue( b.compareTo(a) < 0 );
       assertTrue( c.compareTo(a) > 0 );
       assertTrue( d.compareTo(a) < 0 );
       assertTrue( e.compareTo(a) < 0 );
       assertTrue( f.compareTo(a) < 0 );
       assertTrue( g.compareTo(a) > 0 );

       assertTrue( s2.compareTo(s1) < 0 );
       assertTrue( s3.compareTo(s1) < 0 );
       assertTrue( s4.compareTo(s1) < 0 );
       assertTrue( s5.compareTo(s1) > 0 );

       // Date comparisons..
       System.out.println("".format("Test CompareTo - Date Compare") );
       System.out.println("   HIVAL  d1 value1="+d1.stringValue1()+"  d1 value2="+d1.stringValue2() );
       System.out.println("   LOVAL  d2 value1="+d2.stringValue1()+"  d2 value2="+d2.stringValue2() );
       System.out.println("  CLOSED  d3 value1="+d3.stringValue1()+"  d3 value2="+d3.stringValue2() );
       System.out.println("     d1.compareTo(d2) = "+d1.compareTo(d2) );
       assertTrue( d1.compareTo(d2) < 0 );
       System.out.println("     d1.compareTo(d3) = "+d1.compareTo(d3) );
       System.out.println("".format("Test CompareTo - Issue 14. String comparisons do not handle mixed null on value2, bad EQUAL result.") );
       if ( d1.compareTo(d3) < 0 )
       {
         fail("Issue 14 resolved?");
         //assertTrue( d1.compareTo(d3) < 0 );
       }
       System.out.println("     d2.compareTo(d3) = "+d2.compareTo(d3) );
       assertTrue( d2.compareTo(d3) > 0 );
    }


    private void npeCheckValue( Range range )
    {
       int    ival;
       double dval;

       try {
         ival =  range.intValue1();
         fail( "Expected exception not thrown - value 1" );
       }
       catch ( NullPointerException npe )
       {
         //System.out.println("NPE caught - ival 1");
       }
       try {
         ival =  range.intValue2();
         fail( "Expected exception not thrown - value2" );
       }
       catch ( NullPointerException npe )
       {
         //System.out.println("NPE caught - ival 2");
       }

       try {
         dval =  range.doubleValue1();
         fail( "Expected exception not thrown - value 1" );
       }
       catch ( NullPointerException npe )
       {
         //System.out.println("NPE caught - dval 1");
       }
       try {
         dval =  range.doubleValue2();
         fail( "Expected exception not thrown - value 1" );
       }
       catch ( NullPointerException npe )
       {
         //System.out.println("NPE caught - dval 2");
       }
    }

    private void failCheckValue1( Range range )
    {
       int    ival;
       double dval;

       try {
         ival =  range.intValue1();
         fail( "Expected exception not thrown." );
       }
       catch ( NumberFormatException nfe )
       {
         assertEquals( "nonnumeric range", nfe.getMessage() );
       }

       try {
         dval =  range.doubleValue1();
         fail( "Expected exception not thrown." );
       }
       catch ( NumberFormatException nfe )
       {
         assertEquals( "nonnumeric range", nfe.getMessage() );
       }
    }

    private void failCheckValue2( Range range )
    {
       int    ival;
       double dval;

       try {
         ival =  range.intValue2();
         fail( "Expected exception not thrown." );
       }
       catch ( NumberFormatException nfe )
       {
         assertEquals( "nonnumeric range", nfe.getMessage() );
       }

       try {
         dval =  range.doubleValue2();
         fail( "Expected exception not thrown." );
       }
       catch ( NumberFormatException nfe )
       {
         assertEquals( "nonnumeric range", nfe.getMessage() );
       }
    }


   /**
    * The various combinations of arguments result in a few expected results for
    * each of the tested fields.. this method consolidates the code.
    *
    * Behaviour outside of expectations or that appear inconsistent are indicated 
    * by Issue statements.  These may or may not be actual issues.
    *
    */
    private void baseTest( RangeType type, String value1, String value2,
                           boolean order, boolean isDate)
    {
      r = null;
      try {
        // Construct Range object.
        if ( exp_constructor.equals("OK") )
        {
           try {
             r = new Range( type, value1, value2, order, isDate );
           }
           catch ( InvalidDateException e )
           {
             fail( e.getMessage() );
           }
           assertNotNull( r );
        }
        else if ( exp_constructor.equals("IAE") )
        { // Illegal Argument Exception 
           try {
             r = new Range( type, value1, value2, order, isDate );
             fail( "Expected Illegal Argument Exception not thrown." );
           }
           catch ( IllegalArgumentException iae )
           {
             if ( verbose )
             {
               if ( testnum > 0 )
                 System.out.println( "".format("Test %3d: Caught IAE: %s", testnum, iae.getMessage()) );
               else
                 System.out.println( "  Caught expected IAE: "+iae.getMessage() );
             }
             return;
           }
           catch ( InvalidDateException ide )
           {
             fail( ide.getMessage() );
           }
           assertNull( r );
        }
        else if ( exp_constructor.equals("IDE") )
        { // Invalid Date Exception 
           try {
             r = new Range( type, value1, value2, order, isDate );
             System.out.println("".format("Test %3d - %s",testnum,"Issue 4: Numeric value is valid Date? Possible DateParser issue.") );
             //fail( "Expected exception not thrown." );
           }
           catch ( InvalidDateException ide )
           {
             assertNull( r );
             return;
           }
        }
      }
      catch ( Exception unexpected )
      {
        fail( "".format("Test %3d - Unexpected exception thrown.. %s",testnum, unexpected.getClass().getSimpleName() ) );
      }

      // Verify numeric flag value
      if ( (type == RangeType.ANY)&&(r.isoDate == true)&&(r.numeric == true ) )
      {
        System.out.println("".format("Test %3d - %s",testnum,"Issue 2: ANY type forces numeric=true for non-numeric (Date) values.") );
        System.out.println("".format("             (for other types, Date is non-numeric.)") );
      }
      assertEquals( exp_numeric, r.numeric );

      // Verify isDate flag value
      assertEquals( exp_isdate, r.isoDate );

      // Verify values
      assertEquals( exp_value1, r.stringValue1() );

      if ( (type == RangeType.LOVAL)||(type == RangeType.HIVAL)||(type == RangeType.ONEVAL) )
        assertNull( r.stringValue2() );

      assertEquals( exp_value2, r.stringValue2() );

    }


}
