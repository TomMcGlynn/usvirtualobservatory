package cfa.vo.sedlib;


/**
 * <p>Java class for dateParam complex type.
 * 
 */
public class DateParam
    extends Param
{
   public DateParam () {}

   public DateParam (String value, String name, String ucd)
   {
      super (value, name, ucd);
   }

   public DateParam (String value)
   {
      super (value);
   }

   public DateParam (DateParam param)
   {
      super (param);
   }

}
