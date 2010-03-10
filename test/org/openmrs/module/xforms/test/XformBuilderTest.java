package org.openmrs.module.xforms.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.module.xforms.XformBuilder;
import org.openmrs.module.xforms.XformConstants;
import org.openmrs.module.xforms.formentry.FormEntryWrapper;
import org.openmrs.module.xforms.util.XformsUtil;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class XformBuilderTest extends BaseModuleContextSensitiveTest{

	public Boolean useInMemoryDatabase() {
		return false;
	}
	
	public void testBuildXformFromFixedFiles() throws Exception {
		
		/*Locale locale = Locale.getDefault();
		System.out.println(locale.getLanguage());
		System.out.println(locale.getDisplayName());
		System.out.println(locale.getCountry());
		System.out.println(locale.getISO3Country());
		System.out.println(locale.getISO3Language());
		System.out.println(locale.getDisplayCountry());
		System.out.println(locale.getDisplayLanguage());
		//System.out.println(locale.setDefault(newLocale));*/
		
		
		
		String res = getFileAsString(new File("result.xml"));
		System.out.println(res.length());
		System.out.println(res);
		
		
		String classpath = getFileAsString(new File("classpath.xml"));
		org.kxml2.kdom.Document doc2 = XformBuilder.getDocument(classpath);
		org.kxml2.kdom.Element root = doc2.getRootElement();
		for(int index = 0; index < root.getChildCount(); index++){
			if(root.getType(index) != org.kxml2.kdom.Element.ELEMENT)
				continue;
			
			org.kxml2.kdom.Element node = (org.kxml2.kdom.Element)root.getChild(index);
			String path = node.getAttributeValue(null, "path");
			if(!path.contains("M2_REPO"))
				continue;
			
			int pos = path.lastIndexOf('/');
			if(pos < 0)
				continue;
			
			path = path.substring(pos);
			path = "M2_REPO" + path;
			node.setAttribute(null, "path", path);
		}
		
		System.out.println(XformBuilder.fromDoc2String(doc2));
		
				
		String templateXml = getFileAsString(new File("template.xml"));
		String schemaXml = getFileAsString(new File("FormEntry.xml"));

		try{
			String xform = XformBuilder.getXform4mStrings(schemaXml, templateXml);
			System.out.println(xform);
			
			org.kxml2.kdom.Document doc = XformBuilder.getDocument(getFileAsString(new File("xml.xml")));
			XformBuilder.createCopy(doc.getRootElement());
			System.out.println(XformBuilder.fromDoc2String(doc));
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	public void testBuildXform() throws Exception {
		authenticate();

		//Load OpenMRS form
		Form form = Context.getFormService().getForm(25);
		System.out.println("form = " + form.getName());

		String schemaXml = XformsUtil.getSchema(form);
		String templateXml = FormEntryWrapper.getFormTemplate(form);//new FormXmlTemplateBuilder(form,FormEntryUtil.getFormAbsoluteUrl(form)).getXmlTemplate(false);
		String xform = XformBuilder.getXform4mStrings(schemaXml, templateXml);

		System.out.println("XForm: \n" + xform);
		File outFile = new File("c:\\xformbuildertest.xml");
		FileWriter out = new FileWriter(outFile);
		out.write(xform);
		out.close();
	}

	public void testMergForms(){
		try{
			String s = "142^xform xform";
			String s2 = String.valueOf(s.charAt(0));
			s2 = s.substring(0,s.indexOf('^'));
			
			String patient = getFileAsString(new File("patient.xml"));
			String encounter = getFileAsString(new File("encounter.xml"));

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document docPatient = db.parse(IOUtils.toInputStream(patient,XformConstants.DEFAULT_CHARACTER_ENCODING));
			Document docEncounter = db.parse(IOUtils.toInputStream(encounter,XformConstants.DEFAULT_CHARACTER_ENCODING));
			
			Document doc = db.newDocument();
			Element root = (Element) doc.createElement("openmrs_data");
			doc.appendChild(root);
			
			Node node = root.getOwnerDocument().adoptNode(docPatient.getDocumentElement());
			root.appendChild(node);
			
			node = root.getOwnerDocument().adoptNode(docEncounter.getDocumentElement());
			root.appendChild(node);
			
			File outFile = new File("output.xml");
			FileWriter out = new FileWriter(outFile);
			out.write(XformsUtil.doc2String(doc));
			out.close();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public void testSplit(){
		String result = "Baganda|1$Bacholi|2$Bagisu|3$Basoga|4$Banyankole|5";
		//split(result,0);

		final char FIELD_SEPARATOR = '|';
		final char RECORD_SEPARATOR = '$';

		String text = result; int beginIndex = 0;
		String displayField = null, valueField = null;
		int pos = text.indexOf(FIELD_SEPARATOR,beginIndex);
		while( pos > 0){
			displayField = text.substring(beginIndex, pos);

			beginIndex = pos+1;
			pos = text.indexOf(RECORD_SEPARATOR, beginIndex);
			if(pos > 0){
				valueField = text.substring(beginIndex, pos);
				//split(text,pos+1);
				beginIndex = pos+1;
				pos = text.indexOf(FIELD_SEPARATOR,beginIndex);
			}
			else{
				valueField = text.substring(beginIndex);
			}
		}
	}

	public static String getFileAsString(File file) {
		try{
			StringBuffer fileData = new StringBuffer(1000);
			//BufferedReader reader = new BufferedReader(new FileReader(file));
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file),XformConstants.DEFAULT_CHARACTER_ENCODING));

			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
				buf = new char[1024];
			}
			reader.close();
			return fileData.toString();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}

	private void split(String text, int beginIndex){
		final char FIELD_SEPARATOR = '|';
		final char RECORD_SEPARATOR = '$';

		String displayField = null, valueField = null;
		int pos = text.indexOf(FIELD_SEPARATOR,beginIndex);
		if(pos > 0){
			displayField = text.substring(beginIndex, pos);

			beginIndex = pos+1;
			pos = text.indexOf(RECORD_SEPARATOR, beginIndex);
			if(pos > 0){
				valueField = text.substring(beginIndex, pos);
				split(text,pos+1);
			}
			else{
				valueField = text.substring(beginIndex);
			}
		}
	}
}
