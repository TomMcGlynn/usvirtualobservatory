using System;
using System.Collections;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Linq;
using System.Web;
using System.Web.Services;
using System.Web.Services.Protocols;
using System.Net;
using System.Xml;
using System.Xml.Linq;
using System.ServiceModel;
using System.ServiceModel.Web;
using System.IO;
using System.Text;
using System.Diagnostics;
using System.Configuration;
using System.Threading;

using log4net;
using JsonFx.Json;

using Utilities;

namespace Mashup
{	
    public class MashupQuery : Mashup 
    {		
		#region Test Driver Methods	
        [WebMethod]
        public void MastNameLookup(string input)
        {
			// Service Params
			MashupRequest r = new MashupRequest();		
			r.service = "Mast.Name.Lookup";
			r.paramss["input"] = input;
			
			base.invoke(r.ToJson());
        }
		
		[WebMethod]
        public void NedNameLookup(string input, string format)
        {
			// Service Params
			MashupRequest r = new MashupRequest();		
			r.service = "Ned.Name.Lookup";
			r.paramss["input"] = input;
			
			// Mashup Params
			r.format = format;
			
			base.invoke(r.ToJson());
        }
		
		[WebMethod]
        public void MashupUrlDownload(string url, string filename, string attachment)
        {
			// Service Params
			MashupRequest r = new MashupRequest();		
			r.service = "Mashup.Url.Download";
			r.paramss["url"] = url;
			r.paramss["filename"] = filename;
			r.paramss["attachment"] = attachment;
			
			base.invoke(r.ToJson());
        }
		
		[WebMethod]
        public void MashupFileDownload(string file, string filename, string attachment)
        {
			// Service Params
			MashupRequest r = new MashupRequest();		
			r.service = "Mashup.File.Download";
			r.paramss["file"] = file;
			r.paramss["filename"] = filename;
			r.paramss["attachment"] = attachment;
			
			base.invoke(r.ToJson());
        }

        [WebMethod]
        public void VoGalexCone(string ra, string dec, string radius, string format)
        {	
			// Service Params
			MashupRequest r = new MashupRequest();
			r.service = "Vo.Galex.Cone";
			r.paramss["ra"] = ra;
			r.paramss["dec"] = dec;
			r.paramss["radius"] = radius;
			
			// Mashup Params
			r.format = format;

			base.invoke(r.ToJson());
        }
		
        [WebMethod]
        public void VoGenericTable(string url, string format)
        {	
			// Service Params
			MashupRequest r = new MashupRequest();
			r.service = "Vo.Generic.Table";
			r.paramss["url"] = url;
			
			// Mashup Params
			r.format = format;
			
			base.invoke(r.ToJson());
        }
		
        [WebMethod]
        public void CsvGenericTable(string url, string format)
        {
			// Service Params
			MashupRequest r = new MashupRequest();
			r.service = "Csv.Generic.Table";
			r.paramss["url"] = url;
			
			// Mashup Params
			r.format = format;
		
			base.invoke(r.ToJson());
        }
		
		[WebMethod]
        public void VoInventoryCone(string ra, string dec, string radius, string format)
        {
			// Service Params
			MashupRequest r = new MashupRequest();
			r.service = "Vo.Inventory.Cone";
			r.paramss["ra"] = ra;
			r.paramss["dec"] = dec;
			r.paramss["radius"] = radius;
			
			// Mashup Params
			r.format = format;
		
			base.invoke(r.ToJson());
        }
		
		[WebMethod]
        public void VoHlaCone(string ra, string dec, string radius, string catalog, string format)
        {
			// Service Params
			MashupRequest r = new MashupRequest();
			r.service = "Vo.Hla.Cone";
			r.paramss["ra"] = ra;
			r.paramss["dec"] = dec;
			r.paramss["radius"] = radius;
			r.paramss["catalog"] = catalog;
			
			// Mashup Params
			r.format = format;
			
			base.invoke(r.ToJson());
        }
		
		[WebMethod]
        public void HlaHubbleVotable(string ra, string dec, string radius, string format)
        {
			// Service Params
			MashupRequest r = new MashupRequest();
			r.service = "Hla.Hubble.Votable";
			r.paramss["ra"] = ra;
			r.paramss["dec"] = dec;
			r.paramss["radius"] = radius;
			
			// Mashup Params
			r.format = format;

			base.invoke(r.ToJson());
        }
		
		[WebMethod]
        public void VoCaomTap(string MASHUP_PARAMS, string format)
        {
			// Service Params
			MashupRequest r = new MashupRequest();
			r.service = "Vo.Caom.Tap";
			
			// Mashup Params
			r.format = format;
			
			base.invoke(r.ToJson());
        }
		
		[WebMethod]
        public void VoHesarcDatascope(string ra, string dec, string radius, string format)
        {
			// Service Params
			MashupRequest r = new MashupRequest();
			r.service = "Vo.Hesarc.Datascope";
			r.paramss["ra"] = ra;
			r.paramss["dec"] = dec;
			r.paramss["radius"] = radius;
			
			// Mashup Params
			r.format = format;

			base.invoke(r.ToJson());
        }
		
		[WebMethod]
        public void MastCaomCone(string ra, string dec, string radius, string format)
        {
			// Service Params
			MashupRequest r = new MashupRequest();
			r.service = "Mast.Caom.Cone";
			r.paramss["ra"] = ra;
			r.paramss["dec"] = dec;
			r.paramss["radius"] = radius;
			
			// Mashup Params
			r.format = format;
			
			base.invoke(r.ToJson());
        }
		
		[WebMethod]
        public void MastCaomProducts(string obsid, string format)
        {
			// Service Params
			MashupRequest r = new MashupRequest();
			r.service = "Mast.Caom.Products";
			r.paramss["obsid"] = obsid;
			
			// Mashup Params
			r.format = format;
			
			base.invoke(r.ToJson());
        }
					
		[WebMethod]
        public void MastDistributionRequest(string filelist, string filename)
        {		
			// Load default parameters
			if (filelist == null || filelist.Trim().Length == 0)
			{
				filelist = 
					"http://galex.stsci.edu/data/GR6/pipe/01-vsn/03000-MISDR1_24278_0266/d/00-visits/0001-img/07-try/MISDR1_24278_0266_0001-asp.fits.gz," +
					"http://galex.stsci.edu/data/GR6/pipe/01-vsn/03000-MISDR1_24278_0266/d/00-visits/0001-img/07-try/MISDR1_24278_0266_0001-aspraw.fits.gz," +
					"http://galex.stsci.edu/data/GR6/pipe/01-vsn/03000-MISDR1_24278_0266/d/00-visits/0001-img/07-try/MISDR1_24278_0266_0001-asprta.fits.gz";
			}
			
			if (filename == null || filename.Trim().Length == 0)
			{
				filename = "distribution";
			}
			
			// Service Params
			MashupRequest r = new MashupRequest();		
			r.service = "Mast.Distribution.Request";

			r.paramss["filelist"] = filelist;
			r.paramss["filename"] = filename;
			
			base.invoke(r.ToJson());
        }

        [WebMethod]
        public void VoGalexSiap(string ra, string dec, string radius, string format)
        {
			// Service Params
			MashupRequest r = new MashupRequest();
			r.service = "Vo.Galex.Siap";
			r.paramss["ra"] = ra;
			r.paramss["dec"] = dec;
			r.paramss["radius"] = radius;
			
			// Mashup Params
			r.format = format;
			
			base.invoke(r.ToJson());
        }
		
		[WebMethod]
        public void MastHlspProject(string MASHUP_PARAMS, string format)
        {
			// Service Params
			MashupRequest r = new MashupRequest();
			r.service = "Mast.Hlsp.Project";
			
			// Mashup Params
			r.format = format;
			
			base.invoke(r.ToJson());
        }
		
		[WebMethod]
        public void MastHlspProducts(string id, string format)
        {
			// Service Params
			MashupRequest r = new MashupRequest();
			r.service = "Mast.Hlsp.Products";
			r.paramss["id"] = id;

			// Mashup Params
			r.format = format;

			base.invoke(r.ToJson());
        }
		
		[WebMethod]
        public void MastGalexTile(string ra, string dec, string radius, string catalog, string maxrecords, string format)
        {
			// Service Params
			MashupRequest r = new MashupRequest();
			r.service = "Mast.Galex.Tile";
			r.paramss["ra"] = ra;
			r.paramss["dec"] = dec;
			r.paramss["radius"] = radius;
			r.paramss["catalog"] = catalog;
			r.paramss["maxrecords"] = maxrecords;
			
			// Mashup Params
			r.format = format;
			
			base.invoke(r.ToJson());
        }
		
		[WebMethod]
        public void MastGalexCatalog(string ra, string dec, string radius, string catalog, string maxrecords, string format)
        {
			// Service Params
			MashupRequest r = new MashupRequest();
			r.service = "Mast.Galex.Catalog";
			r.paramss["ra"] = ra;
			r.paramss["dec"] = dec;
			r.paramss["radius"] = radius;
			r.paramss["catalog"] = catalog;
			r.paramss["maxrecords"] = maxrecords;
			
			// Mashup Params
			r.format = format;
	
			base.invoke(r.ToJson());
        }
		
		[WebMethod]
        public void MastGalexSdssCatalog(string ra, string dec, string radius, string catalog, string mission, string maxrecords, string format)
        {
			// Service Params
			MashupRequest r = new MashupRequest();
			r.service = "Mast.Galex.Sdss.Catalog";
			r.paramss["ra"] = ra;
			r.paramss["dec"] = dec;
			r.paramss["radius"] = radius;
			r.paramss["catalog"] = catalog;
			r.paramss["mission"] = mission;
			r.paramss["maxrecords"] = maxrecords;
			
			// Mashup Params
			r.format = format;
		
			base.invoke(r.ToJson());
        }
		
		[WebMethod]
        public void CaomConeVotable(string ra, string dec, string radius, string format)
        {
			// Service Params
			MashupRequest r = new MashupRequest();
			r.service = "Caom.Cone.Votable";
			r.paramss["ra"] = ra;
			r.paramss["dec"] = dec;
			r.paramss["radius"] = radius;
			
			// Mashup Params
			r.format = format;
			
			base.invoke(r.ToJson());
        }
		
		[WebMethod]
        public void AdsConeVotable(string ra, string dec, string radius, string format)
        {
			// Service Params
			MashupRequest r = new MashupRequest();
			r.service = "Ads.Cone.Votable";
			r.paramss["ra"] = ra;
			r.paramss["dec"] = dec;
			r.paramss["radius"] = radius;
			
			// Mashup Params
			r.format = format;
			
			base.invoke(r.ToJson());
        }
		
		[WebMethod]
        public void GalexSiapVotable(string MASHUP_PARAMS, string format)
        {
			// Service params
			MashupRequest r = new MashupRequest();
			r.service = "Galex.Siap.Votable";
			
			// Mashup Params
			r.format = format;
			
			base.invoke(r.ToJson());
        }

		[WebMethod]
        public void GalexPhotonListQuery(string query, string format)
        {
			// Service params
			MashupRequest r = new MashupRequest();
			r.service = "Galex.Photon.List.Query";
			r.clearcache = "true";
			r.paramss["query"] = query;
			
			// Mashup Params
			r.format = format;
			
			base.invoke(r.ToJson());
        }

		[WebMethod]
        public void GalexPhotonListNuv(string ra, string dec, string radius, string timestart, string timeend, string flag, string format)
        {
			// Service params
			MashupRequest r = new MashupRequest();
			r.service = "Galex.Photon.List.Nuv";
			r.paramss["ra"] = ra;
			r.paramss["dec"] = dec;
			r.paramss["radius"] = radius;
			r.paramss["timestart"] = timestart;
			r.paramss["timeend"] = timeend;
			r.paramss["flag"] = flag;
			
			// Mashup Params
			r.clearcache = "true";
			r.format = format;
			
			base.invoke(r.ToJson());
        }

		[WebMethod]
        public void GalexPhotonListFuv(string ra, string dec, string radius, string timestart, string timeend, string flag, string format)
        {
			// Service params
			MashupRequest r = new MashupRequest();
			r.service = "Galex.Photon.List.Fuv";
			r.paramss["ra"] = ra;
			r.paramss["dec"] = dec;
			r.paramss["radius"] = radius;
			r.paramss["timestart"] = timestart;
			r.paramss["timeend"] = timeend;
			r.paramss["flag"] = flag;
			
			// Mashup Params
			r.clearcache = "true";
			r.format = format;

			base.invoke(r.ToJson());
        }
		
		[WebMethod]
        public void MashupTestHttpProxy(string url)
        {
			// Service Params
			MashupRequest r = new MashupRequest();
			r.paramss["url"] = url;
			r.service = "Mashup.Test.HttpProxy";
			
			base.invoke(r.ToJson());
        }

		[WebMethod]
        public void MashupTableExporter(string filename, string filetype, string format)
        {		
			// Service Params
			MashupRequest r = new MashupRequest();
			r.service = "Mashup.Table.Exporter";
			
			// Mashup Params
			r.filename = filename;
			r.filetype = filetype;
			r.data = jsonTable;
			r.format = format;
			r.clearcache = "true";
						
			base.invoke(r.ToJson());
        }
		#endregion 
		    
		private static string jsonTable = 
		"{\"name\":\"0.0.0\"," + 
			"\"Columns\":[ " + 
			"  {\"text\": \"objid\", \"dataIndex\": \"objid\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"objid\",\"vot.ucd\": \"ID_MAIN\",\"vot.datatype\": \"char\",\"vot.name\": \"objid\"}}," + 
			"  {\"text\": \"iauname\", \"dataIndex\": \"iauname\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"iauname\",\"vot.ucd\": \"?\",\"vot.datatype\": \"char\",\"vot.name\": \"iauname\"}}," + 
			"  {\"text\": \"ra\", \"dataIndex\": \"ra\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"ra\",\"vot.ucd\": \"?\",\"vot.datatype\": \"double\",\"vot.name\": \"ra\"}}," + 
			"  {\"text\": \"dec\", \"dataIndex\": \"dec\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"dec\",\"vot.ucd\": \"?\",\"vot.datatype\": \"double\",\"vot.name\": \"dec\"}}," + 
			"  {\"text\": \"e_bv\", \"dataIndex\": \"e_bv\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"e_bv\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"e_bv\"}}," + 
			"  {\"text\": \"nuv_artifact\", \"dataIndex\": \"nuv_artifact\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"nuv_artifact\",\"vot.ucd\": \"?\",\"vot.datatype\": \"int32\",\"vot.name\": \"nuv_artifact\"}}," + 
			"  {\"text\": \"fuv_artifact\", \"dataIndex\": \"fuv_artifact\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"fuv_artifact\",\"vot.ucd\": \"?\",\"vot.datatype\": \"int32\",\"vot.name\": \"fuv_artifact\"}}," + 
			"  {\"text\": \"nuv_flags\", \"dataIndex\": \"nuv_flags\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"nuv_flags\",\"vot.ucd\": \"?\",\"vot.datatype\": \"int16\",\"vot.name\": \"nuv_flags\"}}," + 
			"  {\"text\": \"fuv_flags\", \"dataIndex\": \"fuv_flags\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"fuv_flags\",\"vot.ucd\": \"?\",\"vot.datatype\": \"int16\",\"vot.name\": \"fuv_flags\"}}," + 
			"  {\"text\": \"nuv_flux\", \"dataIndex\": \"nuv_flux\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"nuv_flux\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"nuv_flux\"}}," + 
			"  {\"text\": \"fuv_flux\", \"dataIndex\": \"fuv_flux\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"fuv_flux\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"fuv_flux\"}}," + 
			"  {\"text\": \"nuv_fluxerr\", \"dataIndex\": \"nuv_fluxerr\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"nuv_fluxerr\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"nuv_fluxerr\"}}," + 
			"  {\"text\": \"fuv_fluxerr\", \"dataIndex\": \"fuv_fluxerr\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"fuv_fluxerr\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"fuv_fluxerr\"}}," + 
			"  {\"text\": \"nuv_fwhm_world\", \"dataIndex\": \"nuv_fwhm_world\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"nuv_fwhm_world\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"nuv_fwhm_world\"}}," + 
			"  {\"text\": \"fuv_fwhm_world\", \"dataIndex\": \"fuv_fwhm_world\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"fuv_fwhm_world\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"fuv_fwhm_world\"}}," + 
			"  {\"text\": \"nuv_mag\", \"dataIndex\": \"nuv_mag\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"nuv_mag\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"nuv_mag\"}}," + 
			"  {\"text\": \"fuv_mag\", \"dataIndex\": \"fuv_mag\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"fuv_mag\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"fuv_mag\"}}," + 
			"  {\"text\": \"nuv_magerr\", \"dataIndex\": \"nuv_magerr\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"nuv_magerr\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"nuv_magerr\"}}," + 
			"  {\"text\": \"fuv_magerr\", \"dataIndex\": \"fuv_magerr\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"fuv_magerr\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"fuv_magerr\"}}," + 
			"  {\"text\": \"nuv_fcat_flux\", \"dataIndex\": \"nuv_fcat_flux\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"nuv_fcat_flux\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"nuv_fcat_flux\"}}," + 
			"  {\"text\": \"fuv_ncat_flux\", \"dataIndex\": \"fuv_ncat_flux\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"fuv_ncat_flux\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"fuv_ncat_flux\"}}," + 
			"  {\"text\": \"nuv_fcat_fluxerr\", \"dataIndex\": \"nuv_fcat_fluxerr\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"nuv_fcat_fluxerr\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"nuv_fcat_fluxerr\"}}," + 
			"  {\"text\": \"fuv_ncat_fluxerr\", \"dataIndex\": \"fuv_ncat_fluxerr\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"fuv_ncat_fluxerr\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"fuv_ncat_fluxerr\"}}," + 
			"  {\"text\": \"nuv_weight\", \"dataIndex\": \"nuv_weight\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"nuv_weight\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"nuv_weight\"}}," + 
			"  {\"text\": \"fuv_weight\", \"dataIndex\": \"fuv_weight\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"fuv_weight\",\"vot.ucd\": \"?\",\"vot.datatype\": \"float\",\"vot.name\": \"fuv_weight\"}}," + 
			"  {\"text\": \"survey\", \"dataIndex\": \"survey\"," + 
			"    \"ExtendedProperties\" : {\"vot.ID\": \"survey\",\"vot.ucd\": \"?\",\"vot.datatype\": \"char\",\"vot.name\": \"survey\"}}" +
			"]," + 
			"\"Fields\":[ " + 
			"  {\"name\": \"objid\", \"type\": \"string\"}," + 
			"  {\"name\": \"iauname\", \"type\": \"string\"}," + 
			"  {\"name\": \"ra\", \"type\": \"float\"}," + 
			"  {\"name\": \"dec\", \"type\": \"float\"}," + 
			"  {\"name\": \"e_bv\", \"type\": \"float\"}," + 
			"  {\"name\": \"nuv_artifact\", \"type\": \"int\"}," + 
			"  {\"name\": \"fuv_artifact\", \"type\": \"int\"}," + 
			"  {\"name\": \"nuv_flags\", \"type\": \"int\"}," + 
			"  {\"name\": \"fuv_flags\", \"type\": \"int\"}," + 
			"  {\"name\": \"nuv_flux\", \"type\": \"float\"}," + 
			"  {\"name\": \"fuv_flux\", \"type\": \"float\"}," + 
			"  {\"name\": \"nuv_fluxerr\", \"type\": \"float\"}," + 
			"  {\"name\": \"fuv_fluxerr\", \"type\": \"float\"}," + 
			"  {\"name\": \"nuv_fwhm_world\", \"type\": \"float\"}," + 
			"  {\"name\": \"fuv_fwhm_world\", \"type\": \"float\"}," + 
			"  {\"name\": \"nuv_mag\", \"type\": \"float\"}," + 
			"  {\"name\": \"fuv_mag\", \"type\": \"float\"}," + 
			"  {\"name\": \"nuv_magerr\", \"type\": \"float\"}," + 
			"  {\"name\": \"fuv_magerr\", \"type\": \"float\"}," + 
			"  {\"name\": \"nuv_fcat_flux\", \"type\": \"float\"}," + 
			"  {\"name\": \"fuv_ncat_flux\", \"type\": \"float\"}," + 
			"  {\"name\": \"nuv_fcat_fluxerr\", \"type\": \"float\"}," + 
			"  {\"name\": \"fuv_ncat_fluxerr\", \"type\": \"float\"}," + 
			"  {\"name\": \"nuv_weight\", \"type\": \"float\"}," + 
			"  {\"name\": \"fuv_weight\", \"type\": \"float\"}," + 
			"  {\"name\": \"survey\", \"type\": \"string\"}" +
			"]," + 
			"\"Rows\":[ " + 
			"  [\"2532925283203297679\",\"GALEX J003559.7-425953\",8.99888621477736,-42.9983250726353,0.0068157,0,256,3,3,2.097009,1.593237,0.1955768,0.1512506,0.002704791,0.002033239,23.096,23.3943,0.1012854,0.1030972,2.152891,1.772928,0.1719466,0.1665996,25424,28596,\"DIS\"]," + 
			"  [\"2532925283203297707\",\"GALEX J003558.6-425954\",8.99432154559548,-42.9985192834603,0.0068157,0,256,3,3,2.297442,2.090112,0.1489189,0.1758012,0.001840609,0.002210505,22.99689,23.09958,0.07039393,0.09134442,3.682231,1.465541,0.201472,0.1294275,25456,28600,\"DIS\"]" +
			"]" + 
		"}";	
	}
}