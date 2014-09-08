package cfa.vo.sedlib.common;

import java.util.HashMap;

/**
 * This class contains constant utypes enumerations and their string value 
   associations.  The class also contains methods to retrieve utypes and
   other associations.

   NOTE: This is class instead of an enumeration because as of
   Java 1.5 you cannot extend the Enum class. Using constants
   was a simple workaround.
 */

public class Utypes
{
    static protected final int max_enum = 349; // an easy way to let sub classes extend the enumerations


    //Enumerations
    static public final int   INVALID_UTYPE    =  -1;
    static public final int   SED              =   0;
    static public final int   SEDTYPE          =   1;
    static public final int   DATAMODEL        =   2;
    static public final int   LENGTH           =   3;
    static public final int   TYPE             =   4;
    static public final int   DATE             =   5;
    static public final int   NSEGMENTS        =   6;
    static public final int   CREATOR          =   7;
    static public final int   CREATORDID       =   8;
    static public final int   SPECTRALMINWAVELENGTH    =   9;
    static public final int   SPECTRALMAXWAVELENGTH    =  10;
    static public final int   TIMESI           =  11;
    static public final int   SPECTRALSI       =  12;
    static public final int   FLUXSI           =  13;
    static public final int   TARGET           =  14;
    static public final int   TARGET_NAME      =  15;
    static public final int   TARGET_DESCRIPTION       =  16;
    static public final int   TARGET_CLASS     =  17;
    static public final int   TARGET_SPECTRALCLASS     =  18;
    static public final int   TARGET_REDSHIFT  =  19;
    static public final int   TARGET_POS       =  20;
    static public final int   TARGET_VARAMPL   =  21;
    static public final int   SEG_CS           =  22;
    static public final int   SEG_CS_ID        =  23;
    static public final int   SEG_CS_HREF      =  24;
    static public final int   SEG_CS_UCD       =  25;
    static public final int   SEG_CS_TYPE      =  26;
    static public final int   SEG_CS_IDREF             =  27;
    static public final int   SEG_CS_SPACEFRAME        =  28;
    static public final int   SEG_CS_SPACEFRAME_ID     =  29;
    static public final int   SEG_CS_SPACEFRAME_NAME       =  30;
    static public final int   SEG_CS_SPACEFRAME_UCD    =  31;
    static public final int   SEG_CS_SPACEFRAME_REFPOS     =  32;
    static public final int   SEG_CS_SPACEFRAME_EQUINOX    =  33;
    static public final int   SEG_CS_TIMEFRAME     =  34;
    static public final int   SEG_CS_TIMEFRAME_ID  =  35;
    static public final int   SEG_CS_TIMEFRAME_NAME    =  36;
    static public final int   SEG_CS_TIMEFRAME_UCD     =  37;
    static public final int   SEG_CS_TIMEFRAME_ZERO    =  38;
    static public final int   SEG_CS_TIMEFRAME_REFPOS  =  39;
    static public final int   SEG_CS_SPECTRALFRAME     =  40;
    static public final int   SEG_CS_SPECTRALFRAME_ID      =  41;
    static public final int   SEG_CS_SPECTRALFRAME_NAME    =  42;
    static public final int   SEG_CS_SPECTRALFRAME_UCD     =  43;
    static public final int   SEG_CS_SPECTRALFRAME_REFPOS      =  44;
    static public final int   SEG_CS_SPECTRALFRAME_REDSHIFT    =  45;
    static public final int   SEG_CS_REDFRAME      =  46;
    static public final int   SEG_CS_REDFRAME_ID       =  47;
    static public final int   SEG_CS_REDFRAME_NAME     =  48;
    static public final int   SEG_CS_REDFRAME_UCD      =  49;
    static public final int   SEG_CS_REDFRAME_REFPOS   =  50;
    static public final int   SEG_CS_REDFRAME_DOPPLERDEF   =  51;
    static public final int   SEG_CS_GENFRAME      =  52;
    static public final int   SEG_CS_GENFRAME_ID       =  53;
    static public final int   SEG_CS_GENFRAME_NAME     =  54;
    static public final int   SEG_CS_GENFRAME_UCD      =  55;
    static public final int   SEG_CS_GENFRAME_REFPOS       =  56;
    static public final int   SEG_CURATION         =  57;
    static public final int   SEG_CURATION_PUBLISHER       =  58;
    static public final int   SEG_CURATION_REF     =  59;
    static public final int   SEG_CURATION_PUBID       =  60;
    static public final int   SEG_CURATION_PUBDID      =  61;
    static public final int   SEG_CURATION_VERSION     =  62;
    static public final int   SEG_CURATION_CONTACT     =  63;
    static public final int   SEG_CURATION_CONTACT_NAME    =  64;
    static public final int   SEG_CURATION_CONTACT_EMAIL   =  65;
    static public final int   SEG_CURATION_RIGHTS      =  66;
    static public final int   SEG_CURATION_DATE        =  67;
    static public final int   SEG_DATAID       =  68;
    static public final int   SEG_DATAID_TITLE     =  69;
    static public final int   SEG_DATAID_CREATOR       =  70;
    static public final int   SEG_DATAID_COLLECTION    =  71;
    static public final int   SEG_DATAID_DATASETID     =  72;
    static public final int   SEG_DATAID_CREATORDID    =  73;
    static public final int   SEG_DATAID_DATE      =  74;
    static public final int   SEG_DATAID_VERSION       =  75;
    static public final int   SEG_DATAID_INSTRUMENT    =  76;
    static public final int   SEG_DATAID_CREATIONTYPE      =  77;
    static public final int   SEG_DATAID_LOGO      =  78;
    static public final int   SEG_DATAID_CONTRIBUTOR       =  79;
    static public final int   SEG_DATAID_DATASOURCE    =  80;
    static public final int   SEG_DATAID_BANDPASS      =  81;
    static public final int   SEG_DD       =  82;
    static public final int   SEG_DD_SNR       =  83;
    static public final int   SEG_DD_REDSHIFT      =  84;
    static public final int   SEG_DD_REDSHIFT_VALUE    =  85;
    static public final int   SEG_DD_REDSHIFT_ACC      =  86;
    static public final int   SEG_DD_REDSHIFT_ACC_STATERR      =  87;
    static public final int   SEG_DD_REDSHIFT_ACC_CONFIDENCE   =  88;
    static public final int   SEG_DD_VARAMPL       =  89;
    static public final int   SEG_DD_REDSHIFT_ACC_BINLOW   =  90;
    static public final int   SEG_DD_REDSHIFT_ACC_BINHIGH      =  91;
    static public final int   SEG_DD_REDSHIFT_ACC_BINSIZE      =  92;
    static public final int   SEG_DD_REDSHIFT_ACC_STATERRLOW   =  93;
    static public final int   SEG_DD_REDSHIFT_ACC_STATERRHIGH  =  94;
    static public final int   SEG_DD_REDSHIFT_ACC_SYSERR   =  95;
    static public final int   SEG_DD_REDSHIFT_QUALITY      =  96;
    static public final int   SEG_DD_REDSHIFT_RESOLUTION   =  97;
    static public final int   SEG_CHAR         =  98;
    static public final int   SEG_CHAR_CHARAXIS        =  99;
    static public final int   SEG_CHAR_CHARAXIS_NAME       = 100;
    static public final int   SEG_CHAR_CHARAXIS_UNIT       = 101;
    static public final int   SEG_CHAR_CHARAXIS_UCD    = 102;
    static public final int   SEG_CHAR_CHARAXIS_COV    = 103;
    static public final int   SEG_CHAR_CHARAXIS_COV_LOC    = 104;
    static public final int   SEG_CHAR_CHARAXIS_COV_LOC_VALUE  = 105;
    static public final int   SEG_CHAR_CHARAXIS_COV_BOUNDS     = 106;
    static public final int   SEG_CHAR_CHARAXIS_COV_BOUNDS_MIN     = 107;
    static public final int   SEG_CHAR_CHARAXIS_COV_BOUNDS_MAX     = 108;
    static public final int   SEG_CHAR_CHARAXIS_COV_BOUNDS_EXTENT  = 109;
    static public final int   SEG_CHAR_CHARAXIS_COV_BOUNDS_START   = 110;
    static public final int   SEG_CHAR_CHARAXIS_COV_BOUNDS_STOP    = 111;
    static public final int   SEG_CHAR_CHARAXIS_SAMPPREC_SAMPPRECREFVAL = 112;
    static public final int   SEG_CHAR_CHARAXIS_COV_SUPPORT    = 113;
    static public final int   SEG_CHAR_CHARAXIS_COV_SUPPORT_AREA   = 114;
    static public final int   SEG_CHAR_CHARAXIS_ACC    = 115;
    static public final int   SEG_CHAR_CHARAXIS_ACC_BINSIZE    = 116;
    static public final int   SEG_CHAR_CHARAXIS_ACC_STATERR    = 117;
    static public final int   SEG_CHAR_CHARAXIS_ACC_SYSERR     = 118;
    static public final int   SEG_CHAR_CHARAXIS_CAL    = 119;
    static public final int   SEG_CHAR_CHARAXIS_RESOLUTION     = 120;
    static public final int   SEG_CHAR_CHARAXIS_SAMPPREC   = 121;
    static public final int   SEG_CHAR_CHARAXIS_SAMPPREC_SAMPEXT   = 122;
    static public final int   SEG_CHAR_FLUXAXIS        = 123;
    static public final int   SEG_CHAR_FLUXAXIS_NAME       = 124;
    static public final int   SEG_CHAR_FLUXAXIS_UNIT       = 125;
    static public final int   SEG_CHAR_FLUXAXIS_UCD    = 126;
    static public final int   SEG_CHAR_FLUXAXIS_ACC    = 127;
    static public final int   SEG_CHAR_FLUXAXIS_ACC_BINLOW     = 128;
    static public final int   SEG_CHAR_FLUXAXIS_ACC_BINHIGH    = 129;
    static public final int   SEG_CHAR_FLUXAXIS_ACC_BINSIZE    = 130;
    static public final int   SEG_CHAR_FLUXAXIS_ACC_STATERRLOW     = 131;
    static public final int   SEG_CHAR_FLUXAXIS_ACC_STATERRHIGH    = 132;
    static public final int   SEG_CHAR_FLUXAXIS_ACC_STATERR    = 133;
    static public final int   SEG_CHAR_FLUXAXIS_ACC_SYSERR     = 134;
    static public final int   SEG_CHAR_FLUXAXIS_ACC_CONFIDENCE     = 135;
    static public final int   SEG_CHAR_FLUXAXIS_CAL    = 136;
    static public final int   SEG_CHAR_FLUXAXIS_COV_LOC_VALUE  = 137;
    static public final int   SEG_CHAR_FLUXAXIS_RESOLUTION     = 138;
    static public final int   SEG_CHAR_FLUXAXIS_SAMPPREC   = 139;
    static public final int   SEG_CHAR_FLUXAXIS_SAMPPREC_SAMPEXT   = 140;
    static public final int   SEG_CHAR_FLUXAXIS_SAMPPREC_SAMPPRECREFVAL = 141;
    static public final int   SEG_CHAR_FLUXAXIS_SAMPPREC_SAMPPRECREFVAL_FILL = 142;
    static public final int   SEG_CHAR_FLUXAXIS_COV    = 143;
    static public final int   SEG_CHAR_FLUXAXIS_COV_LOC    = 144;
    static public final int   SEG_CHAR_FLUXAXIS_COV_LOC_RESOLUTION = 145;
    static public final int   SEG_CHAR_FLUXAXIS_COV_LOC_ACC    = 146;
    static public final int   SEG_CHAR_FLUXAXIS_COV_LOC_ACC_BINSIZE    = 147;
    static public final int   SEG_CHAR_FLUXAXIS_COV_LOC_ACC_BINLOW = 148;
    static public final int   SEG_CHAR_FLUXAXIS_COV_LOC_ACC_BINHIGH    = 149;
    static public final int   SEG_CHAR_FLUXAXIS_COV_LOC_ACC_STATERR    = 150;
    static public final int   SEG_CHAR_FLUXAXIS_COV_LOC_ACC_STATERRLOW = 151;
    static public final int   SEG_CHAR_FLUXAXIS_COV_LOC_ACC_STATERRHIGH = 152;
    static public final int   SEG_CHAR_FLUXAXIS_COV_LOC_ACC_CONFIDENCE = 153;
    static public final int   SEG_CHAR_FLUXAXIS_COV_LOC_ACC_SYSERR = 154;
    static public final int   SEG_CHAR_FLUXAXIS_COV_BOUNDS     = 155;
    static public final int   SEG_CHAR_FLUXAXIS_COV_BOUNDS_EXTENT  = 156;
    static public final int   SEG_CHAR_FLUXAXIS_COV_BOUNDS_MIN     = 157;
    static public final int   SEG_CHAR_FLUXAXIS_COV_BOUNDS_MAX     = 158;
    static public final int   SEG_CHAR_FLUXAXIS_COV_SUPPORT    = 159;
    static public final int   SEG_CHAR_FLUXAXIS_COV_SUPPORT_RANGE  = 160;
    static public final int   SEG_CHAR_FLUXAXIS_COV_SUPPORT_AREA   = 161;
    static public final int   SEG_CHAR_FLUXAXIS_COV_SUPPORT_EXTENT = 162;
    static public final int   SEG_CHAR_SPECTRALAXIS    = 163;
    static public final int   SEG_CHAR_SPECTRALAXIS_NAME   = 164;
    static public final int   SEG_CHAR_SPECTRALAXIS_UNIT   = 165;
    static public final int   SEG_CHAR_SPECTRALAXIS_UCD    = 166;
    static public final int   SEG_CHAR_SPECTRALAXIS_COV    = 167;
    static public final int   SEG_CHAR_SPECTRALAXIS_COV_LOC    = 168;
    static public final int   SEG_CHAR_SPECTRALAXIS_COV_LOC_VALUE  = 169;
    static public final int   SEG_CHAR_SPECTRALAXIS_COV_LOC_ACC    = 170;
    static public final int   SEG_CHAR_SPECTRALAXIS_COV_LOC_ACC_BINLOW = 171;
    static public final int   SEG_CHAR_SPECTRALAXIS_COV_LOC_ACC_BINHIGH = 172;
    static public final int   SEG_CHAR_SPECTRALAXIS_COV_LOC_ACC_BINSIZE = 173;
    static public final int   SEG_CHAR_SPECTRALAXIS_COV_LOC_ACC_STATERRLOW = 174;
    static public final int   SEG_CHAR_SPECTRALAXIS_COV_LOC_ACC_STATERRHIGH = 175;
    static public final int   SEG_CHAR_SPECTRALAXIS_COV_LOC_ACC_STATERR = 176;
    static public final int   SEG_CHAR_SPECTRALAXIS_COV_LOC_ACC_SYSERR = 177;
    static public final int   SEG_CHAR_SPECTRALAXIS_COV_LOC_ACC_CONFIDENCE = 178;
    static public final int   SEG_CHAR_SPECTRALAXIS_COV_LOC_RESOLUTION = 179;
    static public final int   SEG_CHAR_SPECTRALAXIS_COV_BOUNDS     = 180;
    static public final int   SEG_CHAR_SPECTRALAXIS_COV_BOUNDS_MIN = 181;
    static public final int   SEG_CHAR_SPECTRALAXIS_COV_BOUNDS_MAX = 182;
    static public final int   SEG_CHAR_SPECTRALAXIS_COV_BOUNDS_EXTENT  = 183;
    static public final int   SEG_CHAR_SPECTRALAXIS_COV_BOUNDS_START   = 184;
    static public final int   SEG_CHAR_SPECTRALAXIS_COV_BOUNDS_STOP    = 185;
    static public final int   SEG_CHAR_SPECTRALAXIS_COV_SUPPORT    = 186;
    static public final int   SEG_CHAR_SPECTRALAXIS_COV_SUPPORT_EXTENT = 187;
    static public final int   SEG_CHAR_SPECTRALAXIS_COV_SUPPORT_RANGE  = 188;
    static public final int   SEG_CHAR_SPECTRALAXIS_COV_SUPPORT_AREA   = 189;
    static public final int   SEG_CHAR_SPECTRALAXIS_SAMPPREC   = 190;
    static public final int   SEG_CHAR_SPECTRALAXIS_SAMPPREC_SAMPEXT   = 191;
    static public final int   SEG_CHAR_SPECTRALAXIS_SAMPPREC_SAMPPRECREFVAL = 192;
    static public final int   SEG_CHAR_SPECTRALAXIS_SAMPPREC_SAMPPRECREFVAL_FILL = 193;
    static public final int   SEG_CHAR_SPECTRALAXIS_ACC    = 194;
    static public final int   SEG_CHAR_SPECTRALAXIS_ACC_BINLOW     = 195;
    static public final int   SEG_CHAR_SPECTRALAXIS_ACC_BINHIGH    = 196;
    static public final int   SEG_CHAR_SPECTRALAXIS_ACC_BINSIZE    = 197;
    static public final int   SEG_CHAR_SPECTRALAXIS_ACC_STATERRLOW = 198;
    static public final int   SEG_CHAR_SPECTRALAXIS_ACC_STATERRHIGH    = 199;
    static public final int   SEG_CHAR_SPECTRALAXIS_ACC_STATERR    = 200;
    static public final int   SEG_CHAR_SPECTRALAXIS_ACC_SYSERR     = 201;
    static public final int   SEG_CHAR_SPECTRALAXIS_ACC_CONFIDENCE = 202;
    static public final int   SEG_CHAR_SPECTRALAXIS_CAL    = 203;
    static public final int   SEG_CHAR_SPECTRALAXIS_RESOLUTION     = 204;
    static public final int   SEG_CHAR_SPECTRALAXIS_RESPOW     = 205;
    static public final int   SEG_CHAR_SPATIALAXIS     = 206;
    static public final int   SEG_CHAR_SPATIALAXIS_NAME    = 207;
    static public final int   SEG_CHAR_SPATIALAXIS_UNIT    = 208;
    static public final int   SEG_CHAR_SPATIALAXIS_UCD     = 209;
    static public final int   SEG_CHAR_SPATIALAXIS_CAL     = 210;
    static public final int   SEG_CHAR_SPATIALAXIS_RESOLUTION  = 211;
    static public final int   SEG_CHAR_SPATIALAXIS_COV     = 212;
    static public final int   SEG_CHAR_SPATIALAXIS_COV_LOC     = 213;
    static public final int   SEG_CHAR_SPATIALAXIS_COV_LOC_VALUE   = 214;
    static public final int   SEG_CHAR_SPATIALAXIS_COV_BOUNDS  = 215;
    static public final int   SEG_CHAR_SPATIALAXIS_COV_BOUNDS_MIN  = 216;
    static public final int   SEG_CHAR_SPATIALAXIS_COV_BOUNDS_MAX  = 217;
    static public final int   SEG_CHAR_SPATIALAXIS_COV_BOUNDS_EXTENT   = 218;
    static public final int   SEG_CHAR_SPATIALAXIS_COV_SUPPORT     = 219;
    static public final int   SEG_CHAR_SPATIALAXIS_COV_SUPPORT_RANGE   = 220;
    static public final int   SEG_CHAR_SPATIALAXIS_COV_SUPPORT_AREA    = 221;
    static public final int   SEG_CHAR_SPATIALAXIS_COV_SUPPORT_EXTENT  = 222;
    static public final int   SEG_CHAR_SPATIALAXIS_ACC     = 223;
    static public final int   SEG_CHAR_SPATIALAXIS_ACC_STATERR     = 224;
    static public final int   SEG_CHAR_SPATIALAXIS_ACC_SYSERR  = 225;
    static public final int   SEG_CHAR_SPATIALAXIS_ACC_STATERRLOW  = 226;
    static public final int   SEG_CHAR_SPATIALAXIS_ACC_STATERRHIGH = 227;
    static public final int   SEG_CHAR_SPATIALAXIS_ACC_BINLOW  = 228;
    static public final int   SEG_CHAR_SPATIALAXIS_ACC_BINHIGH     = 229;
    static public final int   SEG_CHAR_SPATIALAXIS_ACC_BINSIZE     = 230;
    static public final int   SEG_CHAR_SPATIALAXIS_ACC_CONFIDENCE  = 231;
    static public final int   SEG_CHAR_SPATIALAXIS_SAMPPREC    = 232;
    static public final int   SEG_CHAR_SPATIALAXIS_SAMPPREC_SAMPEXT    = 233;
    static public final int   SEG_CHAR_SPATIALAXIS_SAMPPREC_SAMPPRECREFVAL = 234;
    static public final int   SEG_CHAR_SPATIALAXIS_SAMPPREC_SAMPPRECREFVAL_FILL = 235;
    static public final int   SEG_CHAR_SPATIALAXIS_COV_LOC_RESOLUTION  = 236;
    static public final int   SEG_CHAR_SPATIALAXIS_COV_LOC_VALUE_RA    = 237;
    static public final int   SEG_CHAR_SPATIALAXIS_COV_LOC_VALUE_DEC   = 238;
    static public final int   SEG_CHAR_SPATIALAXIS_COV_LOC_ACC     = 239;
    static public final int   SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_BINSIZE = 240;
    static public final int   SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_BINLOW  = 241;
    static public final int   SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_BINHIGH = 242;
    static public final int   SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_STATERR = 243;
    static public final int   SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_STATERRLOW = 244;
    static public final int   SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_STATERRHIGH = 245;
    static public final int   SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_CONFIDENCE = 246;
    static public final int   SEG_CHAR_SPATIALAXIS_COV_LOC_ACC_SYSERR  = 247;
    static public final int   SEG_CHAR_TIMEAXIS        = 248;
    static public final int   SEG_CHAR_TIMEAXIS_NAME       = 249;
    static public final int   SEG_CHAR_TIMEAXIS_UNIT       = 250;
    static public final int   SEG_CHAR_TIMEAXIS_UCD    = 251;
    static public final int   SEG_CHAR_TIMEAXIS_COV    = 252;
    static public final int   SEG_CHAR_TIMEAXIS_COV_LOC    = 253;
    static public final int   SEG_CHAR_TIMEAXIS_COV_LOC_VALUE  = 254;
    static public final int   SEG_CHAR_TIMEAXIS_COV_LOC_ACC    = 255;
    static public final int   SEG_CHAR_TIMEAXIS_COV_LOC_ACC_BINSIZE    = 256;
    static public final int   SEG_CHAR_TIMEAXIS_COV_LOC_ACC_BINLOW = 257;
    static public final int   SEG_CHAR_TIMEAXIS_COV_LOC_ACC_BINHIGH    = 258;
    static public final int   SEG_CHAR_TIMEAXIS_COV_LOC_ACC_STATERR    = 259;
    static public final int   SEG_CHAR_TIMEAXIS_COV_LOC_ACC_STATERRLOW = 260;
    static public final int   SEG_CHAR_TIMEAXIS_COV_LOC_ACC_STATERRHIGH = 261;
    static public final int   SEG_CHAR_TIMEAXIS_COV_LOC_ACC_CONFIDENCE = 262;
    static public final int   SEG_CHAR_TIMEAXIS_COV_LOC_ACC_SYSERR = 263;
    static public final int   SEG_CHAR_TIMEAXIS_COV_BOUNDS     = 264;
    static public final int   SEG_CHAR_TIMEAXIS_COV_BOUNDS_MIN     = 265;
    static public final int   SEG_CHAR_TIMEAXIS_COV_BOUNDS_MAX     = 266;
    static public final int   SEG_CHAR_TIMEAXIS_COV_BOUNDS_EXTENT  = 267;
    static public final int   SEG_CHAR_TIMEAXIS_COV_BOUNDS_START   = 268;
    static public final int   SEG_CHAR_TIMEAXIS_COV_BOUNDS_STOP    = 269;
    static public final int   SEG_CHAR_TIMEAXIS_COV_SUPPORT    = 270;
    static public final int   SEG_CHAR_TIMEAXIS_COV_SUPPORT_AREA   = 271;
    static public final int   SEG_CHAR_TIMEAXIS_SAMPPREC_SAMPPRECREFVAL_FILL = 272;
    static public final int   SEG_CHAR_TIMEAXIS_ACC    = 273;
    static public final int   SEG_CHAR_TIMEAXIS_ACC_BINLOW     = 274;
    static public final int   SEG_CHAR_TIMEAXIS_ACC_BINHIGH    = 275;
    static public final int   SEG_CHAR_TIMEAXIS_ACC_BINSIZE    = 276;
    static public final int   SEG_CHAR_TIMEAXIS_ACC_STATERR    = 277;
    static public final int   SEG_CHAR_TIMEAXIS_ACC_SYSERR     = 278;
    static public final int   SEG_CHAR_TIMEAXIS_ACC_STATERRLOW     = 279;
    static public final int   SEG_CHAR_TIMEAXIS_ACC_STATERRHIGH    = 280;
    static public final int   SEG_CHAR_TIMEAXIS_ACC_CONFIDENCE     = 281;
    static public final int   SEG_CHAR_TIMEAXIS_CAL    = 282;
    static public final int   SEG_CHAR_TIMEAXIS_RESOLUTION     = 283;
    static public final int   SEG_CHAR_TIMEAXIS_SAMPPREC   = 284;
    static public final int   SEG_CHAR_TIMEAXIS_SAMPPREC_SAMPEXT   = 285;
    static public final int   SEG_CHAR_TIMEAXIS_SAMPPREC_SAMPPRECREFVAL = 286;
    static public final int   SEG_CHAR_TIMEAXIS_COV_SUPPORT_EXTENT = 287;
    static public final int   SEG_CHAR_TIMEAXIS_COV_LOC_RESOLUTION = 288;
    static public final int   SEG_DATA_TIMEAXIS_ACC_CONFIDENCE     = 289;
    static public final int   SEG_CHAR_TIMEAXIS_COV_SUPPORT_RANGE  = 290;
    static public final int   SEG_DATA         = 291;
    static public final int   SEG_DATA_FLUXAXIS        = 292;
    static public final int   SEG_DATA_FLUXAXIS_VALUE      = 293;
    static public final int   SEG_DATA_FLUXAXIS_QUALITY    = 294;
    static public final int   SEG_DATA_FLUXAXIS_ACC    = 295;
    static public final int   SEG_DATA_FLUXAXIS_ACC_BINLOW     = 296;
    static public final int   SEG_DATA_FLUXAXIS_ACC_BINHIGH    = 297;
    static public final int   SEG_DATA_FLUXAXIS_ACC_BINSIZE    = 298;
    static public final int   SEG_DATA_FLUXAXIS_ACC_STATERRLOW     = 299;
    static public final int   SEG_DATA_FLUXAXIS_ACC_STATERRHIGH    = 300;
    static public final int   SEG_DATA_FLUXAXIS_ACC_STATERR    = 301;
    static public final int   SEG_DATA_FLUXAXIS_ACC_SYSERR     = 302;
    static public final int   SEG_DATA_FLUXAXIS_ACC_CONFIDENCE     = 303;
    static public final int   SEG_DATA_FLUXAXIS_RESOLUTION     = 304;
    static public final int   SEG_DATA_SPECTRALAXIS    = 305;
    static public final int   SEG_DATA_SPECTRALAXIS_VALUE      = 306;
    static public final int   SEG_DATA_SPECTRALAXIS_ACC    = 307;
    static public final int   SEG_DATA_SPECTRALAXIS_ACC_STATERR    = 308;
    static public final int   SEG_DATA_SPECTRALAXIS_ACC_STATERRLOW = 309;
    static public final int   SEG_DATA_SPECTRALAXIS_ACC_STATERRHIGH    = 310;
    static public final int   SEG_DATA_SPECTRALAXIS_ACC_BINLOW     = 311;
    static public final int   SEG_DATA_SPECTRALAXIS_ACC_BINHIGH    = 312;
    static public final int   SEG_DATA_SPECTRALAXIS_ACC_BINSIZE    = 313;
    static public final int   SEG_DATA_SPECTRALAXIS_ACC_SYSERR     = 314;
    static public final int   SEG_DATA_SPECTRALAXIS_RESOLUTION     = 315;
    static public final int   SEG_DATA_SPECTRALAXIS_ACC_CONFIDENCE = 316;
    static public final int   SEG_DATA_TIMEAXIS        = 317;
    static public final int   SEG_DATA_TIMEAXIS_VALUE      = 318;
    static public final int   SEG_DATA_TIMEAXIS_ACC    = 319;
    static public final int   SEG_DATA_TIMEAXIS_ACC_STATERR    = 320;
    static public final int   SEG_DATA_TIMEAXIS_ACC_STATERRLOW     = 321;
    static public final int   SEG_DATA_TIMEAXIS_ACC_STATERRHIGH    = 322;
    static public final int   SEG_DATA_TIMEAXIS_ACC_SYSERR     = 323;
    static public final int   SEG_DATA_TIMEAXIS_ACC_BINLOW     = 324;
    static public final int   SEG_DATA_TIMEAXIS_ACC_BINHIGH    = 325;
    static public final int   SEG_DATA_TIMEAXIS_ACC_BINSIZE    = 326;
    static public final int   SEG_DATA_TIMEAXIS_RESOLUTION     = 327;
    static public final int   SEG_DATA_BGM         = 328;
    static public final int   SEG_DATA_BGM_VALUE       = 329;
    static public final int   SEG_DATA_BGM_QUALITY     = 330;
    static public final int   SEG_DATA_BGM_ACC     = 331;
    static public final int   SEG_DATA_BGM_ACC_STATERR     = 332;
    static public final int   SEG_DATA_BGM_ACC_STATERRLOW      = 333;
    static public final int   SEG_DATA_BGM_ACC_STATERRHIGH     = 334;
    static public final int   SEG_DATA_BGM_ACC_SYSERR      = 335;
    static public final int   SEG_DATA_BGM_RESOLUTION      = 336;
    static public final int   SEG_DATA_BGM_ACC_BINLOW      = 337;
    static public final int   SEG_DATA_BGM_ACC_BINHIGH     = 338;
    static public final int   SEG_DATA_BGM_ACC_BINSIZE     = 339;
    static public final int   SEG_DATA_BGM_ACC_CONFIDENCE      = 340;
    static public final int   CUSTOM       = 341;
    static public final int   SEG          = 342;
    static public final int   SEG_CHAR_CHARAXIS_ACC_BINLOW     = 343;
    static public final int   SEG_CHAR_CHARAXIS_ACC_BINHIGH    = 344;
    static public final int   SEG_CHAR_CHARAXIS_ACC_STATERRLOW     = 345;
    static public final int   SEG_CHAR_CHARAXIS_ACC_STATERRHIGH    = 346;
    static public final int   SEG_CHAR_CHARAXIS_ACC_CONFIDENCE     = 347;
    static public final int   SEG_CHAR_CHARAXIS_SAMPPREC_SAMPPRECREFVAL_FILL = 348;



    final static protected String[] name = {
        "Spectrum.Sed",
        "Spectrum.SedType",
        "Spectrum.DataModel",
        "Spectrum.Length",
        "Spectrum.Type",
        "Spectrum.Date",
        "Spectrum.NSegments",
        "Spectrum.Creator",
        "Spectrum.CreatorDID",
        "Spectrum.SpectralMinimumWavelength",
        "Spectrum.SpectralMaximumWavelength",
        "Spectrum.TimeSI",
        "Spectrum.SpectralSI",
        "Spectrum.FluxSI",
        "Spectrum.Target",
        "Spectrum.Target.Name",
        "Spectrum.Target.Description",
        "Spectrum.Target.Class",
        "Spectrum.Target.SpectralClass",
        "Spectrum.Target.Redshift",
        "Spectrum.Target.Pos",
        "Spectrum.Target.VarAmpl",
        "Spectrum.CoordSys",
        "Spectrum.CoordSys.ID",
        "Spectrum.CoordSys.Href",
        "Spectrum.CoordSys.Ucd",
        "Spectrum.CoordSys.Type",
        "Spectrum.CoordSys.Idref",
        "Spectrum.CoordSys.SpaceFrame",
        "Spectrum.CoordSys.SpaceFrame.ID",
        "Spectrum.CoordSys.SpaceFrame.Name",
        "Spectrum.CoordSys.SpaceFrame.UCD",
        "Spectrum.CoordSys.SpaceFrame.RefPos",
        "Spectrum.CoordSys.SpaceFrame.Equinox",
        "Spectrum.CoordSys.TimeFrame",
        "Spectrum.CoordSys.TimeFrame.ID",
        "Spectrum.CoordSys.TimeFrame.Name",
        "Spectrum.CoordSys.TimeFrame.UCD",
        "Spectrum.CoordSys.TimeFrame.Zero",
        "Spectrum.CoordSys.TimeFrame.RefPos",
        "Spectrum.CoordSys.SpectralFrame",
        "Spectrum.CoordSys.SpectralFrame.ID",
        "Spectrum.CoordSys.SpectralFrame.Name",
        "Spectrum.CoordSys.SpectralFrame.UCD",
        "Spectrum.CoordSys.SpectralFrame.RefPos",
        "Spectrum.CoordSys.SpectralFrame.Redshift",
        "Spectrum.CoordSys.RedshiftFrame",
        "Spectrum.CoordSys.RedshiftFrame.ID",
        "Spectrum.CoordSys.RedshiftFrame.Name",
        "Spectrum.CoordSys.RedshiftFrame.UCD",
        "Spectrum.CoordSys.RedshiftFrame.RefPos",
        "Spectrum.CoordSys.RedshiftFrame.DopplerDefinition",
        "Spectrum.CoordSys.GenericCoordFrame",
        "Spectrum.CoordSys.GenericCoordFrame.ID",
        "Spectrum.CoordSys.GenericCoordFrame.Name",
        "Spectrum.CoordSys.GenericCoordFrame.UCD",
        "Spectrum.CoordSys.GenericCoordFrame.RefPos",
        "Spectrum.Curation",
        "Spectrum.Curation.Publisher",
        "Spectrum.Curation.Reference",
        "Spectrum.Curation.PublisherID",
        "Spectrum.Curation.PublisherDID",
        "Spectrum.Curation.Version",
        "Spectrum.Curation.Contact",
        "Spectrum.Curation.Contact.Name",
        "Spectrum.Curation.Contact.Email",
        "Spectrum.Curation.Rights",
        "Spectrum.Curation.Date",
        "Spectrum.DataID",
        "Spectrum.DataID.Title",
        "Spectrum.DataID.Creator",
        "Spectrum.DataID.Collection",
        "Spectrum.DataID.DatasetID",
        "Spectrum.DataID.CreatorDID",
        "Spectrum.DataID.Date",
        "Spectrum.DataID.Version",
        "Spectrum.DataID.Instrument",
        "Spectrum.DataID.CreationType",
        "Spectrum.DataID.Logo",
        "Spectrum.DataID.Contributor",
        "Spectrum.DataID.DataSource",
        "Spectrum.DataID.Bandpass",
        "Spectrum.Derived",
        "Spectrum.Derived.SNR",
        "Spectrum.Derived.Redshift",
        "Spectrum.Derived.Redshift.Value",
        "Spectrum.Derived.Redshift.Accuracy",
        "Spectrum.Derived.Redshift.Accuracy.StatError",
        "Spectrum.Derived.Redshift.Accuracy.Confidence",
        "Spectrum.Derived.VarAmpl",
        "Spectrum.Derived.Redshift.Accuracy.BinLow",
        "Spectrum.Derived.Redshift.Accuracy.BinHigh",
        "Spectrum.Derived.Redshift.Accuracy.BinSize",
        "Spectrum.Derived.Redshift.Accuracy.StatErrLow",
        "Spectrum.Derived.Redshift.Accuracy.StatErrHigh",
        "Spectrum.Derived.Redshift.Accuracy.SysError",
        "Spectrum.Derived.Redshift.Quality",
        "Spectrum.Derived.Redshift.Resolution",
        "Spectrum.Char",
        "Spectrum.Char.CharAxis",
        "Spectrum.Char.CharAxis.Name",
        "Spectrum.Char.CharAxis.Unit",
        "Spectrum.Char.CharAxis.UCD",
        "Spectrum.Char.CharAxis.Coverage",
        "Spectrum.Char.CharAxis.Coverage.Location",
        "Spectrum.Char.CharAxis.Coverage.Location.Value",
        "Spectrum.Char.CharAxis.Coverage.Bounds",
        "Spectrum.Char.CharAxis.Coverage.Bounds.Min",
        "Spectrum.Char.CharAxis.Coverage.Bounds.Max",
        "Spectrum.Char.CharAxis.Coverage.Bounds.Extent",
        "Spectrum.Char.CharAxis.Coverage.Bounds.Start",
        "Spectrum.Char.CharAxis.Coverage.Bounds.Stop",
        "Spectrum.Char.CharAxis.SamplingPrecision.SamplingPrecisionRefVal",
        "Spectrum.Char.CharAxis.Coverage.Support",
        "Spectrum.Char.CharAxis.Coverage.Support.Area",
        "Spectrum.Char.CharAxis.Accuracy",
        "Spectrum.Char.CharAxis.Accuracy.BinSize",
        "Spectrum.Char.CharAxis.Accuracy.StatError",
        "Spectrum.Char.CharAxis.Accuracy.SysError",
        "Spectrum.Char.CharAxis.Calibration",
        "Spectrum.Char.CharAxis.Resolution",
        "Spectrum.Char.CharAxis.SamplingPrecision",
        "Spectrum.Char.CharAxis.SamplingPrecision.SamplingExtent",
        "Spectrum.Char.FluxAxis",
        "Spectrum.Char.FluxAxis.Name",
        "Spectrum.Char.FluxAxis.Unit",
        "Spectrum.Char.FluxAxis.UCD",
        "Spectrum.Char.FluxAxis.Accuracy",
        "Spectrum.Char.FluxAxis.Accuracy.BinLow",
        "Spectrum.Char.FluxAxis.Accuracy.BinHigh",
        "Spectrum.Char.FluxAxis.Accuracy.BinSize",
        "Spectrum.Char.FluxAxis.Accuracy.StatErrLow",
        "Spectrum.Char.FluxAxis.Accuracy.StatErrHigh",
        "Spectrum.Char.FluxAxis.Accuracy.StatError",
        "Spectrum.Char.FluxAxis.Accuracy.SysError",
        "Spectrum.Char.FluxAxis.Accuracy.Confidence",
        "Spectrum.Char.FluxAxis.Calibration",
        "Spectrum.Char.FluxAxis.Coverage.Location.Value",
        "Spectrum.Char.FluxAxis.Resolution",
        "Spectrum.Char.FluxAxis.SamplingPrecision",
        "Spectrum.Char.FluxAxis.SamplingPrecision.SamplingExtent",
        "Spectrum.Char.FluxAxis.SamplingPrecision.SamplingPrecisionRefVal",
        "Spectrum.Char.FluxAxis.SamplingPrecision.SamplingPrecisionRefVal.FillFactor",
        "Spectrum.Char.FluxAxis.Coverage",
        "Spectrum.Char.FluxAxis.Coverage.Location",
        "Spectrum.Char.FluxAxis.Coverage.Location.Resolution",
        "Spectrum.Char.FluxAxis.Coverage.Location.Accuracy",
        "Spectrum.Char.FluxAxis.Coverage.Location.Accuracy.BinSize",
        "Spectrum.Char.FluxAxis.Coverage.Location.Accuracy.BinLow",
        "Spectrum.Char.FluxAxis.Coverage.Location.Accuracy.BinHigh",
        "Spectrum.Char.FluxAxis.Coverage.Location.Accuracy.StatError",
        "Spectrum.Char.FluxAxis.Coverage.Location.Accuracy.StatErrLow",
        "Spectrum.Char.FluxAxis.Coverage.Location.Accuracy.StatErrHigh",
        "Spectrum.Char.FluxAxis.Coverage.Location.Accuracy.Confidence",
        "Spectrum.Char.FluxAxis.Coverage.Location.Accuracy.SysError",
        "Spectrum.Char.FluxAxis.Coverage.Bounds",
        "Spectrum.Char.FluxAxis.Coverage.Bounds.Extent",
        "Spectrum.Char.FluxAxis.Coverage.Bounds.Min",
        "Spectrum.Char.FluxAxis.Coverage.Bounds.Max",
        "Spectrum.Char.FluxAxis.Coverage.Support",
        "Spectrum.Char.FluxAxis.Coverage.Support.Range",
        "Spectrum.Char.FluxAxis.Coverage.Support.Area",
        "Spectrum.Char.FluxAxis.Coverage.Support.Extent",
        "Spectrum.Char.SpectralAxis",
        "Spectrum.Char.SpectralAxis.Name",
        "Spectrum.Char.SpectralAxis.Unit",
        "Spectrum.Char.SpectralAxis.UCD",
        "Spectrum.Char.SpectralAxis.Coverage",
        "Spectrum.Char.SpectralAxis.Coverage.Location",
        "Spectrum.Char.SpectralAxis.Coverage.Location.Value",
        "Spectrum.Char.SpectralAxis.Coverage.Location.Accuracy",
        "Spectrum.Char.SpectralAxis.Coverage.Location.Accuracy.BinLow",
        "Spectrum.Char.SpectralAxis.Coverage.Location.Accuracy.BinHigh",
        "Spectrum.Char.SpectralAxis.Coverage.Location.Accuracy.BinSize",
        "Spectrum.Char.SpectralAxis.Coverage.Location.Accuracy.StatErrLow",
        "Spectrum.Char.SpectralAxis.Coverage.Location.Accuracy.StatErrHigh",
        "Spectrum.Char.SpectralAxis.Coverage.Location.Accuracy.StatError",
        "Spectrum.Char.SpectralAxis.Coverage.Location.Accuracy.SysError",
        "Spectrum.Char.SpectralAxis.Coverage.Location.Accuracy.Confidence",
        "Spectrum.Char.SpectralAxis.Coverage.Location.Resolution",
        "Spectrum.Char.SpectralAxis.Coverage.Bounds",
        "Spectrum.Char.SpectralAxis.Coverage.Bounds.Min",
        "Spectrum.Char.SpectralAxis.Coverage.Bounds.Max",
        "Spectrum.Char.SpectralAxis.Coverage.Bounds.Extent",
        "Spectrum.Char.SpectralAxis.Coverage.Bounds.Start",
        "Spectrum.Char.SpectralAxis.Coverage.Bounds.Stop",
        "Spectrum.Char.SpectralAxis.Coverage.Support",
        "Spectrum.Char.SpectralAxis.Coverage.Support.Extent",
        "Spectrum.Char.SpectralAxis.Coverage.Support.Range",
        "Spectrum.Char.SpectralAxis.Coverage.Support.Area",
        "Spectrum.Char.SpectralAxis.SamplingPrecision",
        "Spectrum.Char.SpectralAxis.SamplingPrecision.SamplingExtent",
        "Spectrum.Char.SpectralAxis.SamplingPrecision.SamplingPrecisionRefVal",
        "Spectrum.Char.SpectralAxis.SamplingPrecision.SamplingPrecisionRefVal.FillFactor",
        "Spectrum.Char.SpectralAxis.Accuracy",
        "Spectrum.Char.SpectralAxis.Accuracy.BinLow",
        "Spectrum.Char.SpectralAxis.Accuracy.BinHigh",
        "Spectrum.Char.SpectralAxis.Accuracy.BinSize",
        "Spectrum.Char.SpectralAxis.Accuracy.StatErrLow",
        "Spectrum.Char.SpectralAxis.Accuracy.StatErrHigh",
        "Spectrum.Char.SpectralAxis.Accuracy.StatError",
        "Spectrum.Char.SpectralAxis.Accuracy.SysError",
        "Spectrum.Char.SpectralAxis.Accuracy.Confidence",
        "Spectrum.Char.SpectralAxis.Calibration",
        "Spectrum.Char.SpectralAxis.Resolution",
        "Spectrum.Char.SpectralAxis.ResPower",
        "Spectrum.Char.SpatialAxis",
        "Spectrum.Char.SpatialAxis.Name",
        "Spectrum.Char.SpatialAxis.Unit",
        "Spectrum.Char.SpatialAxis.UCD",
        "Spectrum.Char.SpatialAxis.Calibration",
        "Spectrum.Char.SpatialAxis.Resolution",
        "Spectrum.Char.SpatialAxis.Coverage",
        "Spectrum.Char.SpatialAxis.Coverage.Location",
        "Spectrum.Char.SpatialAxis.Coverage.Location.Value",
        "Spectrum.Char.SpatialAxis.Coverage.Bounds",
        "Spectrum.Char.SpatialAxis.Coverage.Bounds.Min",
        "Spectrum.Char.SpatialAxis.Coverage.Bounds.Max",
        "Spectrum.Char.SpatialAxis.Coverage.Bounds.Extent",
        "Spectrum.Char.SpatialAxis.Coverage.Support",
        "Spectrum.Char.SpatialAxis.Coverage.Support.Range",
        "Spectrum.Char.SpatialAxis.Coverage.Support.Area",
        "Spectrum.Char.SpatialAxis.Coverage.Support.Extent",
        "Spectrum.Char.SpatialAxis.Accuracy",
        "Spectrum.Char.SpatialAxis.Accuracy.StatError",
        "Spectrum.Char.SpatialAxis.Accuracy.SysError",
        "Spectrum.Char.SpatialAxis.Accuracy.StatErrLow",
        "Spectrum.Char.SpatialAxis.Accuracy.StatErrHigh",
        "Spectrum.Char.SpatialAxis.Accuracy.BinLow",
        "Spectrum.Char.SpatialAxis.Accuracy.BinHigh",
        "Spectrum.Char.SpatialAxis.Accuracy.BinSize",
        "Spectrum.Char.SpatialAxis.Accuracy.Confidence",
        "Spectrum.Char.SpatialAxis.SamplingPrecision",
        "Spectrum.Char.SpatialAxis.SamplingPrecision.SamplingExtent",
        "Spectrum.Char.SpatialAxis.SamplingPrecision.SamplingPrecisionRefVal",
        "Spectrum.Char.SpatialAxis.SamplingPrecision.SamplingPrecisionRefVal.FillFactor",
        "Spectrum.Char.SpatialAxis.Coverage.Location.Resolution",
        "Spectrum.Char.SpatialAxis.Coverage.Location.Value.RA",
        "Spectrum.Char.SpatialAxis.Coverage.Location.Value.DEC",
        "Spectrum.Char.SpatialAxis.Coverage.Location.Accuracy",
        "Spectrum.Char.SpatialAxis.Coverage.Location.Accuracy.BinSize",
        "Spectrum.Char.SpatialAxis.Coverage.Location.Accuracy.BinLow",
        "Spectrum.Char.SpatialAxis.Coverage.Location.Accuracy.BinHigh",
        "Spectrum.Char.SpatialAxis.Coverage.Location.Accuracy.StatError",
        "Spectrum.Char.SpatialAxis.Coverage.Location.Accuracy.StatErrLow",
        "Spectrum.Char.SpatialAxis.Coverage.Location.Accuracy.StatErrHigh",
        "Spectrum.Char.SpatialAxis.Coverage.Location.Accuracy.Confidence",
        "Spectrum.Char.SpatialAxis.Coverage.Location.Accuracy.SysError",
        "Spectrum.Char.TimeAxis",
        "Spectrum.Char.TimeAxis.Name",
        "Spectrum.Char.TimeAxis.Unit",
        "Spectrum.Char.TimeAxis.UCD",
        "Spectrum.Char.TimeAxis.Coverage",
        "Spectrum.Char.TimeAxis.Coverage.Location",
        "Spectrum.Char.TimeAxis.Coverage.Location.Value",
        "Spectrum.Char.TimeAxis.Coverage.Location.Accuracy",
        "Spectrum.Char.TimeAxis.Coverage.Location.Accuracy.BinSize",
        "Spectrum.Char.TimeAxis.Coverage.Location.Accuracy.BinLow",
        "Spectrum.Char.TimeAxis.Coverage.Location.Accuracy.BinHigh",
        "Spectrum.Char.TimeAxis.Coverage.Location.Accuracy.StatError",
        "Spectrum.Char.TimeAxis.Coverage.Location.Accuracy.StatErrLow",
        "Spectrum.Char.TimeAxis.Coverage.Location.Accuracy.StatErrHigh",
        "Spectrum.Char.TimeAxis.Coverage.Location.Accuracy.Confidence",
        "Spectrum.Char.TimeAxis.Coverage.Location.Accuracy.SysError",
        "Spectrum.Char.TimeAxis.Coverage.Bounds",
        "Spectrum.Char.TimeAxis.Coverage.Bounds.Min",
        "Spectrum.Char.TimeAxis.Coverage.Bounds.Max",
        "Spectrum.Char.TimeAxis.Coverage.Bounds.Extent",
        "Spectrum.Char.TimeAxis.Coverage.Bounds.Start",
        "Spectrum.Char.TimeAxis.Coverage.Bounds.Stop",
        "Spectrum.Char.TimeAxis.Coverage.Support",
        "Spectrum.Char.TimeAxis.Coverage.Support.Area",
        "Spectrum.Char.TimeAxis.SamplingPrecision.SamplingPrecisionRefVal.FillFactor",
        "Spectrum.Char.TimeAxis.Accuracy",
        "Spectrum.Char.TimeAxis.Accuracy.BinLow",
        "Spectrum.Char.TimeAxis.Accuracy.BinHigh",
        "Spectrum.Char.TimeAxis.Accuracy.BinSize",
        "Spectrum.Char.TimeAxis.Accuracy.StatError",
        "Spectrum.Char.TimeAxis.Accuracy.SysError",
        "Spectrum.Char.TimeAxis.Accuracy.StatErrLow",
        "Spectrum.Char.TimeAxis.Accuracy.StatErrHigh",
        "Spectrum.Char.TimeAxis.Accuracy.Confidence",
        "Spectrum.Char.TimeAxis.Calibration",
        "Spectrum.Char.TimeAxis.Resolution",
        "Spectrum.Char.TimeAxis.SamplingPrecision",
        "Spectrum.Char.TimeAxis.SamplingPrecision.SamplingExtent",
        "Spectrum.Char.TimeAxis.SamplingPrecision.SamplingPrecisionRefVal",
        "Spectrum.Char.TimeAxis.Coverage.Support.Extent",
        "Spectrum.Char.TimeAxis.Coverage.Location.Resolution",
        "Spectrum.Data.TimeAxis.Accuracy.Confidence",
        "Spectrum.Char.TimeAxis.Coverage.Support.Range",
        "Spectrum.Data",
        "Spectrum.Data.FluxAxis",
        "Spectrum.Data.FluxAxis.Value",
        "Spectrum.Data.FluxAxis.Quality",
        "Spectrum.Data.FluxAxis.Accuracy",
        "Spectrum.Data.FluxAxis.Accuracy.BinLow",
        "Spectrum.Data.FluxAxis.Accuracy.BinHigh",
        "Spectrum.Data.FluxAxis.Accuracy.BinSize",
        "Spectrum.Data.FluxAxis.Accuracy.StatErrLow",
        "Spectrum.Data.FluxAxis.Accuracy.StatErrHigh",
        "Spectrum.Data.FluxAxis.Accuracy.StatError",
        "Spectrum.Data.FluxAxis.Accuracy.SysError",
        "Spectrum.Data.FluxAxis.Accuracy.Confidence",
        "Spectrum.Data.FluxAxis.Resolution",
        "Spectrum.Data.SpectralAxis",
        "Spectrum.Data.SpectralAxis.Value",
        "Spectrum.Data.SpectralAxis.Accuracy",
        "Spectrum.Data.SpectralAxis.Accuracy.StatError",
        "Spectrum.Data.SpectralAxis.Accuracy.StatErrLow",
        "Spectrum.Data.SpectralAxis.Accuracy.StatErrHigh",
        "Spectrum.Data.SpectralAxis.Accuracy.BinLow",
        "Spectrum.Data.SpectralAxis.Accuracy.BinHigh",
        "Spectrum.Data.SpectralAxis.Accuracy.BinSize",
        "Spectrum.Data.SpectralAxis.Accuracy.SysError",
        "Spectrum.Data.SpectralAxis.Resolution",
        "Spectrum.Data.SpectralAxis.Accuracy.Confidence",
        "Spectrum.Data.TimeAxis",
        "Spectrum.Data.TimeAxis.Value",
        "Spectrum.Data.TimeAxis.Accuracy",
        "Spectrum.Data.TimeAxis.Accuracy.StatError",
        "Spectrum.Data.TimeAxis.Accuracy.StatErrLow",
        "Spectrum.Data.TimeAxis.Accuracy.StatErrHigh",
        "Spectrum.Data.TimeAxis.Accuracy.SysError",
        "Spectrum.Data.TimeAxis.Accuracy.BinLow",
        "Spectrum.Data.TimeAxis.Accuracy.BinHigh",
        "Spectrum.Data.TimeAxis.Accuracy.BinSize",
        "Spectrum.Data.TimeAxis.Resolution",
        "Spectrum.Data.BackgroundModel",
        "Spectrum.Data.BackgroundModel.Value",
        "Spectrum.Data.BackgroundModel.Quality",
        "Spectrum.Data.BackgroundModel.Accuracy",
        "Spectrum.Data.BackgroundModel.Accuracy.StatError",
        "Spectrum.Data.BackgroundModel.Accuracy.StatErrLow",
        "Spectrum.Data.BackgroundModel.Accuracy.StatErrHigh",
        "Spectrum.Data.BackgroundModel.Accuracy.SysError",
        "Spectrum.Data.BackgroundModel.Resolution",
        "Spectrum.Data.BackgroundModel.Accuracy.BinLow",
        "Spectrum.Data.BackgroundModel.Accuracy.BinHigh",
        "Spectrum.Data.BackgroundModel.Accuracy.BinSize",
        "Spectrum.Data.BackgroundModel.Accuracy.Confidence",
        "Spectrum.Custom",
        "Spectrum.Segment",
        "Spectrum.Char.CharAxis.Accuracy.BinLow",
        "Spectrum.Char.CharAxis.Accuracy.BinHigh",
        "Spectrum.Char.CharAxis.Accuracy.StatErrLow",
        "Spectrum.Char.CharAxis.Accuracy.StatErrHigh",
        "Spectrum.Char.CharAxis.Accuracy.Confidence",
        "Spectrum.Char.CharAxis.SamplingPrecision.SamplingPrecisionRefVal.FillFactor",
        };

    static protected String[] ucd = new String[max_enum];

    static protected HashMap<String,Integer> nameMap = new HashMap<String,Integer> ();
    static
    {
        for (int ii=0; ii<name.length; ii++)
            nameMap.put (name[ii], ii);

        ucd[LENGTH] = "meta.number";
        ucd[TIMESI] = "time;arith.zp";
        ucd[SEG_CS_SPACEFRAME_EQUINOX] = "time.equinox;pos.frame";
        ucd[SEG_CS_TIMEFRAME_NAME] = "time.scale";
        ucd[SEG_CS_TIMEFRAME_ZERO] = "time;arith.zp";
        ucd[SEG_CS_TIMEFRAME_REFPOS] = "time.scale";
        ucd[SEG_CURATION_PUBLISHER] = "meta.curation";
        ucd[SEG_CURATION_PUBID] = "meta.ref.url;meta.curation";
        ucd[SEG_CURATION_VERSION] = "meta.version;meta.curation";
        ucd[SEG_CURATION_REF] = "meta.bib.bibcode";
        ucd[SEG_CURATION_CONTACT_NAME] = "meta.bib.author;meta.curation";
        ucd[SEG_CURATION_CONTACT_EMAIL] = "meta.ref.url;meta.email";
        ucd[SEG_CURATION_PUBDID] = "meta.ref.url;meta.curation";
        ucd[SEG_DATAID_TITLE] = "meta.title;meta.dataset";
        ucd[SEG_DATAID_DATASETID] = "meta.id;meta.dataset";
        ucd[SEG_DATAID_CREATORDID] = "meta.id";
        ucd[SEG_DATAID_DATE] = "time;meta.dataset";
        ucd[SEG_DATAID_VERSION] = "meta.version;meta.dataset";
        ucd[SEG_DATAID_INSTRUMENT] = "meta.id;instr";
        ucd[SEG_DATAID_BANDPASS] = "intr.bandpass";
        ucd[SEG_DATAID_LOGO] = "meta.ref.url";
        ucd[SEG_DD_SNR] = "stat.snr";
        ucd[SEG_DD_VARAMPL] = "src.var.amplitude;arith.ratio";
/*        ucd[SEG_DD_REDSHIFT_STATERR] = "stat.error;src.redshift";
*/
        ucd[TARGET_NAME] = "meta.id;src";
        ucd[TARGET_DESCRIPTION] = "meta.note;src";
        ucd[TARGET_CLASS] = "src.class";
        ucd[TARGET_SPECTRALCLASS] = "src.spType";
        ucd[TARGET_REDSHIFT] = "src.redshift";
        ucd[TARGET_POS] = "pos.eq;src";
        ucd[TARGET_VARAMPL] = "src.var.amplitude";

        ucd[SEG_CHAR_SPATIALAXIS_NAME] = "meta.id";
        ucd[SEG_CHAR_SPATIALAXIS_UCD] = "meta.ucd";
        ucd[SEG_CHAR_SPATIALAXIS_UNIT] = "meta.unit";
//        ucd[SEG_CHAR_FLUXAXIS_CAL] = "?";
        ucd[SEG_CHAR_SPATIALAXIS_CAL] = "meta.code.qual";
        ucd[SEG_CHAR_TIMEAXIS_CAL] = "meta.code.qual";
        ucd[SEG_CHAR_SPECTRALAXIS_CAL] = "meta.code.qual";
        ucd[SEG_CHAR_SPATIALAXIS_COV_LOC_VALUE] = "pos.eq";
        ucd[SEG_CHAR_SPATIALAXIS_COV_BOUNDS_EXTENT] = "instr.fov";
//        ucd[SEG_CHAR_SPATIALAXIS_COV_SUPPORT_AREA] = "?";
        ucd[SEG_CHAR_SPATIALAXIS_COV_SUPPORT_EXTENT] = "instr.fov";
        ucd[SEG_CHAR_TIMEAXIS_COV_LOC_VALUE] = "time.epoch";
        ucd[SEG_CHAR_TIMEAXIS_COV_BOUNDS_EXTENT] = "time.duration";
        ucd[SEG_CHAR_TIMEAXIS_COV_BOUNDS_START] = "time.start;obs.exposure";
        ucd[SEG_CHAR_TIMEAXIS_COV_BOUNDS_STOP] = "time.stop;obs.exposure";
        ucd[SEG_CHAR_TIMEAXIS_COV_BOUNDS_MIN] = "time.start;obs.exposure";
        ucd[SEG_CHAR_TIMEAXIS_COV_BOUNDS_MAX] = "time.stop;obs.exposure";
        ucd[SEG_CHAR_TIMEAXIS_COV_SUPPORT_EXTENT] = "time.duration;obs.exposure";
        ucd[SEG_CHAR_SPECTRALAXIS_COV_LOC_VALUE] = "instr.bandpass";
        ucd[SEG_CHAR_SPECTRALAXIS_COV_BOUNDS_EXTENT] = "instr.bandwidth";
        ucd[SEG_CHAR_SPECTRALAXIS_COV_BOUNDS_MIN] = "em.*;stat.min";
        ucd[SEG_CHAR_SPECTRALAXIS_COV_BOUNDS_MAX] = "em.*;stat.max";
        ucd[SEG_CHAR_SPECTRALAXIS_COV_BOUNDS_START] = "em.*;stat.min";
        ucd[SEG_CHAR_SPECTRALAXIS_COV_BOUNDS_STOP] = "em.*;stat.max";

        ucd[SEG_CHAR_SPECTRALAXIS_COV_SUPPORT_EXTENT] = "instr.bandwidth";
        ucd[SEG_CHAR_SPECTRALAXIS_SAMPPREC_SAMPEXT] = "em.*;spect.binSize";
        ucd[SEG_CHAR_SPATIALAXIS_SAMPPREC_SAMPEXT] = "instr.pixel";
        ucd[SEG_CHAR_TIMEAXIS_SAMPPREC_SAMPEXT] = "time.interval";
        ucd[SEG_CHAR_SPATIALAXIS_SAMPPREC_SAMPPRECREFVAL_FILL] = "stat.fill;pos.eq";
        ucd[SEG_CHAR_SPECTRALAXIS_SAMPPREC_SAMPPRECREFVAL_FILL] = "stat.fill;em.*";
        ucd[SEG_CHAR_TIMEAXIS_SAMPPREC_SAMPPRECREFVAL_FILL] = "time;stat.fill;time";
        ucd[SEG_CHAR_FLUXAXIS_ACC_STATERR] = "stat.error;phot.flux.density;em.*";
        ucd[SEG_CHAR_FLUXAXIS_ACC_SYSERR] = "stat.error.sys;phot.flux.density;em.*";
        ucd[SEG_CHAR_SPECTRALAXIS_ACC_BINSIZE] = "em.*;spect.binSize";
        ucd[SEG_CHAR_SPECTRALAXIS_ACC_STATERR] = "stat.error;em.*";
        ucd[SEG_CHAR_SPECTRALAXIS_ACC_SYSERR] = "stat.error;em.*";
        ucd[SEG_CHAR_SPECTRALAXIS_RESOLUTION] = "spect.resolution;em.*";
        ucd[SEG_CHAR_SPECTRALAXIS_RESPOW] = "spect.resolution";
        ucd[SEG_CHAR_TIMEAXIS_ACC_BINSIZE] = "time.interval";
        ucd[SEG_CHAR_TIMEAXIS_ACC_STATERR] = "stat.error;time";
        ucd[SEG_CHAR_TIMEAXIS_ACC_SYSERR] = "stat.error.sys;time";
        ucd[SEG_CHAR_TIMEAXIS_RESOLUTION] = "time.resolution";
        ucd[SEG_CHAR_SPATIALAXIS_ACC_STATERR] = "stat.error;pos.eq";
        ucd[SEG_CHAR_SPATIALAXIS_ACC_SYSERR] = "stat.error.sys;pos.eq";
        ucd[SEG_CHAR_SPATIALAXIS_RESOLUTION] = "pos.angResolution";
        ucd[SEG_DATA_FLUXAXIS_ACC_STATERR] = "stat.error;phot.flux.density;em.*";
        ucd[SEG_DATA_FLUXAXIS_ACC_STATERRLOW] = "stat.error;phot.flux.density;em.*;stat.min";
        ucd[SEG_DATA_FLUXAXIS_ACC_STATERRHIGH] = "stat.error;phot.flux.density;em.*;stat.max";
        ucd[SEG_DATA_FLUXAXIS_ACC_SYSERR] = "stat.error.sys;phot.flux.density;em.*";
        ucd[SEG_DATA_FLUXAXIS_QUALITY] = "meta.code.qual;phot.flux.density;em.*";
        ucd[SEG_DATA_SPECTRALAXIS_ACC_BINSIZE] = "em.*;spect.binSize";
        ucd[SEG_DATA_SPECTRALAXIS_ACC_BINLOW] = "em.*;stat.min";
        ucd[SEG_DATA_SPECTRALAXIS_ACC_BINHIGH] = "em.*;stat.max";
        ucd[SEG_DATA_SPECTRALAXIS_ACC_STATERR] = "stat.error;em.*";
        ucd[SEG_DATA_SPECTRALAXIS_ACC_STATERRLOW] = "stat.error;em.*;stat.min";
        ucd[SEG_DATA_SPECTRALAXIS_ACC_STATERRHIGH] = "stat.error;em.*;stat.max";
        ucd[SEG_DATA_SPECTRALAXIS_ACC_SYSERR] = "stat.error.sys;em.*";
        ucd[SEG_DATA_SPECTRALAXIS_RESOLUTION] = "spect.resolution;em.*";
        ucd[SEG_DATA_TIMEAXIS_ACC_BINSIZE] = "time.interval";
        ucd[SEG_DATA_TIMEAXIS_ACC_BINLOW] = "time;stat.min";
        ucd[SEG_DATA_TIMEAXIS_ACC_BINHIGH] = "time;stat.max";
        ucd[SEG_DATA_TIMEAXIS_ACC_STATERR] = "stat.error;time";
        ucd[SEG_DATA_TIMEAXIS_ACC_STATERRLOW] = "stat.error;time;stat.min";
        ucd[SEG_DATA_TIMEAXIS_ACC_STATERRHIGH] = "stat.error;time;stat.max";
        ucd[SEG_DATA_TIMEAXIS_ACC_SYSERR] = "stat.error.sys;time";
        ucd[SEG_DATA_TIMEAXIS_RESOLUTION] = "time.resolution";
        ucd[SEG_DATA_BGM_ACC_STATERR] = "stat.error;phot.flux.density;em.*";
        ucd[SEG_DATA_BGM_ACC_STATERRLOW] = "stat.error;phot.flux.density;em.*;stat.min";
        ucd[SEG_DATA_BGM_ACC_STATERRHIGH] = "stat.error;phot.flux.density;em.*;stat.max";
        ucd[SEG_DATA_BGM_ACC_SYSERR] = "stat.error.sys;phot.flux.density;em.*";
        ucd[SEG_DATA_BGM_QUALITY] = "meta.code.qual;phot.flux.density;em.*";

    }

    /**
     * Gets the utype enumeration from string name.
     *
     */
    static public int getUtypeFromString (String name)
    {
    	return nameMap.get (name);
    }
    
    /**
     * Gets the string representation of a utype enumeration.
     *
     */
    static public String getName (int utype) 
    {
        return name[utype];
    }


    /**
     * Override wild card sections of the ucd with a valid value. Currently only
     * "em.*" value can be overridden. For em valid override values include
     * wave, freq, and ener.
     * @param utype
     *   the enumerated utype
     * @param ucdBase
     *   the ucd section to override (em)
     * @param override
     *   the String to override the wildcard (wave, freq, and ener)
     *   
     *
     */
    static public String overrideUcd (int utype, String ucdBase, String override)
    {
        String ucdName;

        if (utype >= ucd.length)
            return null;

        ucdName =  ucd[utype];
        
        if (ucdName == null)
            return null;

        if (ucdBase.equalsIgnoreCase ("em"))
        {
            if (ucdName.matches ("^.*em\\.\\*.*$"))
            {
                if (override.equalsIgnoreCase ("wave"))
                    ucdName = ucdName.replaceFirst ("em\\.\\*", "em.wl");
                else if (override.equalsIgnoreCase ("freq"))
                    ucdName = ucdName.replaceFirst ("em\\.\\*", "em.freq");
                else if (override.equalsIgnoreCase ("ener"))
                    ucdName = ucdName.replaceFirst ("em\\.\\*", "em.energy");
            }
        }
        else
            ucdName = null;

        return ucdName;

    }

    /**
     * Gets the ucd associated with the specified utype enumeration.
     *
     */
    static public String getUcd (int utype)
    {
        return ucd[utype];
    }

    /**
     * Compares string version the utypes. Case is ignored.
     *
     */
    static public boolean compareUtypes (String utype1, String utype2)
    {
        return utype1.equalsIgnoreCase (utype2);
    }

    /**
     *  Returns the last part of the utype. This includes whatever follows
     *  the last '.'.
     */
    static public String getLastPartOfUtype( String utype )
    {
        if ( utype == null )
        {
            return null;
        }

        String[] parts = utype.split( "[.]" );

        return parts[ parts.length - 1 ];
    }

    /**
     *  Retrieve an enumeration which is the combination of 
     *  of two inputs. The first argument is the base of the 
     *  new enumeration. The second argument is the desired
     *  end.
     */
    static public int mergeUtypes (int baseUtype, int suffixUtype) 
    {
        String baseUtypeName = name[baseUtype];
        String suffixUtypeName = name[suffixUtype];
        String newUtypeName = name[baseUtype];

        String[] baseParts = baseUtypeName.split( "[.]" );
        String[] suffixParts = suffixUtypeName.split ( "[.]" );

        for (int ii=baseParts.length; ii < suffixParts.length; ii++)
           newUtypeName = newUtypeName.concat ("."+suffixParts[ii]);

        return getUtypeFromString (newUtypeName);
    }

    /**
     * Gets the number of utypes.
     *
     */
    static public int getNumberOfUtypes ()
    {
        return max_enum;
    }

}


