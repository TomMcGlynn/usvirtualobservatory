package net.ivoa.datascope;

import java.util.HashMap;

/** This class generates nicer short names for some services.
 *  The xxx is used to indicate that a service should not be used.
 */
public class SNFixer {

    static String[][] converts = new String[][] {
  {"abell", "Abell"},
  {"acrs", "ACRS"},
  {"ascagis", "ASCA"},
  {"ascagps", "ASCA"},
  {"ascalss", "ASCA"},
  {"ascamaster", "ASCA"},
  {"ascasis", "ASCA"},
  {"asiagosn99", "AsiagoSNC"},
  {"atnfpulsar", "ATNF"},
  {"batsegrb", "BATSE"},
  {"batseocc", "BATSE"},
  {"batten", "BATTEN"},
  {"bbxrt", "BBXRT"},
  {"bestars", "Be"},
  {"bmwhricat", "BMW-HRI"},
  {"bootesdf", "Bootes"},
  {"bsc5p", "BSC"},
  {"cabscat", "ChrAcBin"},
  {"cbatpicagn", "GRO/Piccinott"},
  {"cgmw", "CG"},
  {"chandfn1ms", "ChanDF"},
  {"chandfs1ms", "CDFS1MS"},
  {"chanmaster", "Chandra"},
  {"chasfrxray", "RASS/Cham"},
  {"cns3", "CNS3"},
  {"cosbmaps", "COS-B"},
  {"cosbraw", "COS-B"},
  {"cpstars", "Ap/Am"},
  {"cvcat", "CVs"},
  {"denisigal", "DENIS/I"},
  {"dixon", "Dixon"},
  {"duerbeck", "Nova"},
  {"dxrbs", "ROSAT/DXRBS"},
  {"egret3", "EGRET"},
  {"egretdata", "GRO/EGRET"},
  {"eingalcat", "Einstein/Gal"},
  {"einlog", "Einstein"},
  {"einopslgal", "Einstein/OGal"},
  {"einstein2e", "Einstein"},
  {"emss", "Einstein/EMSS"},
  {"esouppsala", "ESO/Uppsala"},
  {"euv", "EUV"},
  {"euvebsl", "EUVE/Bright"},
  {"euvecat2", "EUVE/2"},
  {"euvemaster", "EUVE"},
  {"euverap2", "EUVE/RAP2"},
  {"euvexrtcat", "EUV/Faint"},
  {"exms", "EXOSAT/Slew"},
  {"exogps", "EXOSAT/Plane"},
  {"exohgls", "EXOSAT/High"},
  {"exomaster", "EXOSAT"},
  {"exss", "Einstein/Ext."},
  {"faust", "FAUST"},
  {"first", "FIRST"},
  {"gb6", "GB6"},
  {"gcvs", "GCVS"},
  {"gcvsegsn", "GCVS/SN"},
  {"gcvsegvars", "GCVS/X-gal"},
  {"gcvsnsvars", "GCVS/?"},
  {"gingalog", "GINGA"},
  {"globclust", "GC"},
  {"gusbad", "BATSE/GUSBAD"},
  {"hbc", "HBC"},
  {"hcg", "Hickson(Gr"},
  {"hcggalaxy", "Hickson(Gal)"},
  {"hdec", "HDEC"},
  {"hic", "HIC"},
  {"hiiregion", "Sharpless"},
  {"hipparcos", "Hipparcos"},
  {"hmxbcat", "HM"},
  {"hrasscat", "RASS/HRC"},
  {"hricfa", "Einstein/HRI"},
  {"HRI", "hrideep"},
  {"hriexo", "HRIEXO"},
  {"hstaec", "HST"},
  {"hyadesxray", "RASS/Hyades"},
  {"infrared", "CIO"},
  {"ipc", "Einstein/IPC"},
  {"ar", "ipcostars"},
  {"t", "ipcultsoft"},
  {"irasfsc", "IRAS"},
  {"iraspsc", "IRAS"},
  {"iraszsurv", "IRAS"},
  {"isolog", "ISO"},
  {"iuelog", "IUE"},
  {"kommersgrb", "Kommers"},
  {"kuehr", "Kuehr"},
  {"lbn", "Lynds(Bright)"},
  {"lbqs", "LBQS"},
  {"ldn", "Lynds(Dark)"},
  {"lmcclustrs", "LMC/Cluster"},
  {"lmchrixray", "HRI/LMC"},
  {"lmcrosxray", "PSPC/LMC"},
  {"lmcxray", "Einstein/LMC"},
  {"lmxbcat", "LMXB"},
  {"m31clustrs", "M31/GC"},
  {"m31rosxray", "ROSAT/M31"},
  {"markarian", "Markarian"},
  {"mcg", "MCG"},
  {"mcksion", "WDw/MS"},
  {"messier", "Messier"},
  {"mitgb6cm", "MIT-GB"},
  {"mrc", "MRC"},
  {"msxpsc", "MSX"},
  {"ngc2000", "NGC"},
  {"nltt", "NLTT"},
  {"north20cm", "North20cm"},
  {"north6cm", "North-6cm"},
  {"nvss", "NVSS"},
  {"openclust", "OpenCluster"},
  {"optical", "Optical"},
  {"orionxray", "HRI/Orion"},
  {"orionxstar", "ORIONXST"},
  {"ostars", "O"},
  {"pg", "PG"},
  {"pkscat90", "Parkes"},
  {"pleiadxray", "PSPC/Pleiades"},
  {"plnebulae", "Plan.Neb."},
  {"pmn", "PMN"},
  {"pmpulsar", "Parkes"},
  {"pmsucat", "PMSU"},
  {"ppm", "PPM"},
  {"pulsar", "Taylor"},
  {"qso", "HB"},
  {"radio", "Radio"},
  {"rassbsc", "RASS/BSC"},
  {"rasscns3", "RASS/CNS3"},
  {"rassdwarf", "RASS/Dwarf"},
  {"rassfsc", "RASS/FSC"},
  {"rassgb", "RASS/GBC"},
  {"rassgiant", "RASS/Giant"},
  {"rasshgsoft", "RASS/Soft"},
  {"rassnorsam", "RASS/North"},
  {"rassob", "RASS/OB"},
  {"rasspublic", "RASS"},
  {"rassvars", "ROSAT/Vars"},
  {"rasswd", "RASS/WDw"},
  {"rbs", "RASS/RBS"},
  {"rc3", "RC3"},
  {"revisedlhs", "LuytenHS"},
  {"rittercv", "Ritter/CV"},
  {"ritterlmxb", "Ritter/LMXB"},
  {"ritterrbin", "Ritter/Bin"},
  {"rosatlog", "ROSATLOG"},
  {"rosatrlq", "ROSAT/RLQ"},
  {"rosatrqq", "ROSAT/RQQ"},
  {"rosatxuv", "ROSAT/REP"},
  {"rosgalclus", "ROSAT/Clust."},
  {"roshri", "ROSAT/HRI"},
  {"rospspc", "ROSAT/PSPC"},
  {"rospublic", "ROSAT"},
  {"roswfc2re", "ROSAT/WFC"},
  {"sao", "SAO"},
  {"sax2to10", "BeSAX"},
  {"saxnfilog", "SAX"},
  {"saxwfclog", "SAX"},
  {"sdssquasar", "SDSS(QSO)"},
  {"shk", "Shk.(Group)"},
  {"shkgalaxy", "Shk.(Gal.)"},
  {"smcclustrs", "SMC/Clust"},
  {"smcrosxray", "PSPC/SMC"},
  {"smcrosxry2", "PSPC/SMC2"},
  {"smcstars2", "SMCSTAR2"},
  {"smcxray", "Einstein/SMC"},
  {"snrgreen", "SNR(Green)"},
  {"sterngrb", "Stern"},
  {"sumss", "SUMSS"},
  {"tartarus", "ASCA/AGN"},
  {"td1", "TD1"},
  {"texas", "Texas"},
  {"twosigma", "Einstein/ETS"},
  {"tycho2", "Tycho-2"},
  {"ugc", "UGC"},
  {"uit", "UIT"},
  {"uzc", "UZC"},
  {"veroncat", "Veron"},
  {"vlanep", "VLA"},
  {"wackerling", "Wackerling"},
  {"warps", "ROSAT/WARPS"},
  {"wbl", "WBL"},
  {"wblgalaxy", "WBL"},
  {"wds", "WDS"},
  {"wenss", "WENSS"},
  {"wfcpoint", "ROSAT/WFC"},
  {"wgacat", "WGACAT"},
  {"woodebcat", "Wood/Bin"},
  {"woolley", "WOOLLEY"},
  {"wrcat", "WR"},
  {"xcopraw", "Copern.X"},
  {"xmmmaster", "XMM"},
  {"xmmssc", "XMM/SSC"},
  {"xray", "X-ray"},
  {"xrbcat", "XRBs"},
  {"xteasmlong", "XTE/ASM"},
  {"xteindex", "RXTE"},
  {"zcat", "Z"},
  {"zwclusters", "Zwicky"},
  {"a1", "XXX"},
  {"a1point", "XXX"},
  {"a2lcpoint", "XXX"},
  {"a2lcscan", "XXX"},
  {"a2led", "XXX"},
  {"a2pic", "XXX"},
  {"a2point", "XXX"},
  {"a2rtraw", "XXX"},
  {"a2specback", "XXX"},
  {"a2spectra", "XXX"},
  {"a3", "XXX"},
  {"a4", "XXX"},
  {"a4spectra", "XXX"},
  {"ariel3a", "XXX"},
  {"ariel5", "XXX"},
  {"ascao", "XXX"},
  {"ascaprspec", "XXX"},
  {"batse4b", "BATSE 4B"},
  {"batsedaily", "xxx"},
  {"batseeocat", "xxx"},
  {"batsepulsr", "xxx"},
  {"batsetrigs", "xxx"},
  {"baxgalclus", "BAX GalClus"},
  {"cdfn2msoid", "Chan/DF2S"},
  {"cfa2s", "CfA Red.S."},
  {"cgroprspec", "xxx"},
  {"cgrotl", "xxx"},
  {"chandfn2ms", "Chan/DF2N"},
  {"chansexoid", "Chan/Seren/Opt"},
  {"chansexsi", "Chan/Seren/ID"},
  {"chianti", "CHIANTI"},
  {"clasxs", "Chan/Syn/LHN"},
  {"cmaimage", "xxx"},
  {"comptel", "xxx"},
  {"crabtime", "xxx"},
  {"cxoxassist", "Chan/XAssist"},
  {"edgeenergy", "Edge Energies"},
  {"eingalclus", "Einstein/Clus"},
  {"exgalemobj", "H&B91"},
  {"exofot", "xxx"},
  {"exolog", "xxx"},
  {"exopubs", "xxx"},
  {"flarestars", "FlareStars"},
  {"fpcsfits", "xxx"},
  {"fuselog", "FUSE"},
  {"gcps", "GCPS"},
  {"gingabgd", "xxx"},
  {"gingalac", "xxx"},
  {"gingamode", "xxx"},
  {"gingaraw", "xxx"},
  {"gpa", "GP8.35/14.35"},
  {"gpsr5", "GPSR5"},
  {"gs", "xxx"},
  {"hete2gcn", "xxx"},
  {"hete2grb", "xxx"},
  {"hrassoptid", "HRASS/Opt"},
  {"hriimage", "xxx"},
  {"hriphot", "xxx"},
  {"hstpaec", "xxx"},
  {"hubbleudf", "UDF"},
  {"ibisgpscat", "IBIS/GPS"},
  {"intbsc", "INTEGRAL/BSC"},
  {"integralao", "xxx"},
  {"intgccat", "IBIS/GC"},
  {"intpublic", "INTEGRAL"},
  {"intrefcat", "INT/RefCat"},
  {"intscw", "xxx"},
  {"intscwpub", "xxx"},
  {"intspiagrb", "xxx"},
  {"ipcdeep", "xxx"},
  {"ipcimage", "xxx"},
  {"ipclxlbol", "xxx"},
  {"ipcphot", "xxx"},
  {"ipcslew", "xxx"},
  {"ipcunscrnd", "xxx"},
  {"ipngrb", "GRB/IPN"},
  {"konus", "xxx"},
  {"le", "xxx"},
  {"lineenergy", "LineEnergy"},
  {"lyngaclust", "LyngaOpenCl"},
  {"m31cxoxray", "Chan/M31"},
  {"m31stars", "M31 Stars"},
  {"m31stars2", "M31 Stars/deep"},
  {"m31xmmxray", "XMM/M31"},
  {"m33xmmxray", "XMM/M33"},
  {"macs", "MACS"},
  {"me", "xxx"},
  {"mggammacat", "M&G Gamma-ray"},
  {"mggammadet", "xxx"},
  {"mpcraw", "xxx"},
  {"noras", "NORAS GalClus"},
  {"oso8alc", "xxx"},
  {"oso8bclc", "xxx"},
  {"oso8pharaw", "xxx"},
  {"oso8rtraw", "xxx"},
  {"osse", "xxx"},
  {"pgc2003", "PGC"},
  {"phebus", "xxx"},
  {"pvogrb", "xxx"},
  {"qorgcat", "QuasarOrg"},
  {"rasscals", "RASS-CALS"},
  {"rassdssagn", "RASS/SDSS AGN"},
  {"rassebcs", "RASS BCS"},
  {"rassmaster", "xxx"},
  {"rbscnvss", "RASS/NVSS"},
  {"reflex", "ROSAT/ESO Clus."},
  {"rixos", "RXOS"},
  {"roshritotal", "ROSAT/HRI"},
  {"rosmaster", "ROSAT"},
  {"rosnepagn", "ROSAT NEP AGN"},
  {"rosnepoid", "ROSAT NEP ID"},
  {"rosprspec", "xxx"},
  {"rospspctotal", "ROSAT/PSPC"},
  {"saisncat", "SAI SN"},
  {"sas2maps", "xxx"},
  {"sas2raw", "xxx"},
  {"sas3ylog", "xxx"},
  {"saxao", "xxx"},
  {"sc", "xxx"},
  {"sdssnbcqsc", "SDSS QSO Cand."},
  {"smcradio", "SMC Radio"},
  {"smcstars", "SMC Stars"},
  {"smmgrs", "xxx"},
  {"spitzmastr", "Spitzer"},
  {"sss", "xxx"},
  {"sssraw", "xxx"},
  {"swiftmastr", "Swift"},
  {"swifttdrss", "xxx"},
  {"tgs", "xxx"},
  {"tgs2", "xxx"},
  {"uhuru4", "xxx"},
  {"ulxrbcat", "ULXRB"},
  {"vela5b", "xxx"},
  {"xmdsvvds4s", "XMM/VIRMOS"},
  {"xmmao", "xxx"},
  {"xmmcfrscat", "XMM/CFRS"},
  {"xmmcfrsoid", "XMM/CFRS ID"},
  {"xmmgps", "XMM/GPS"},
  {"xmmprspec", "xxx"},
  {"xmmxassist", "XMM/XAssist"},
  {"xteao", "xxx"},
  {"xteasmquick", "xxx"},
  {"xteassagn", "xxx"},
  {"xteasscat", "xxx"},
  {"xtemaster", "RXTE"},
  {"xteslew", "xxx"},
  {"optical", "xxx"},
  {"rasspublic", "xxx"},
  {"rospublic", "xxx"}
    };
    static HashMap<String, String> hash = new HashMap<String, String>();
    
    static {
	for (String[] trial: converts) {
	    hash.put(trial[0], trial[1]);
	}
    }
    
    static String fix(String input) {
	if (input == null) {
	    return input;
	}
	if (hash.containsKey(input.toLowerCase())) {
	    return hash.get(input.toLowerCase());
	} else {
	    return input;
	}
    }
}
