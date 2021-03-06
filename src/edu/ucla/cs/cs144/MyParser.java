/* CS144
 *
 * Parser skeleton for processing item-???.xml files. Must be compiled in
 * JDK 1.5 or above.
 *
 * Instructions:
 *
 * This program processes all files passed on the command line (to parse
 * an entire diectory, type "java MyParser myFiles/*.xml" at the shell).
 *
 * At the point noted below, an individual XML file has been parsed into a
 * DOM Document node. You should fill in code to process the node. Java's
 * interface for the Document Object Model (DOM) is in package
 * org.w3c.dom. The documentation is available online at
 *
 * http://java.sun.com/j2se/1.5.0/docs/api/index.html
 *
 * A tutorial of Java's XML Parsing can be found at:
 *
 * http://java.sun.com/webservices/jaxp/
 *
 * Some auxiliary methods have been written for you. You may find them
 * useful.
 */



/*---------------------*/
/* HIGH LEVEL OVERVIEW */
/*---------------------*/

/*---------------------------------------*/
//  main() -> processFile() -> parseItem()
/*---------------------------------------*/


// main(args) 
// {
//     for (each XML File in args) 
//     {   
//         processFile ( File )
//         {
//             initiate BufferedWriters for items.dat, users.dat, categories.dat, bids.dat  
//
//             for (each Item in File ) 
//             {
//                 parseItem (Item)
//                 {
//                     extract data from Item using getElementByTagNameNR, getElementText, getElementTextByTagNameNR
//                     write out item data to items.dat
//                     write out user data to users.dat
//                     write out category data to categories.dat
//                     write out bid data to bids.dat
//                 }
//             }
//             close BufferedWriters
//         }
//     }
// }



package edu.ucla.cs.cs144;

import java.io.*;
import java.text.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;
import java.lang.Object;


class MyParser {
    
    static final String columnSeparator = "|*|";
    static DocumentBuilder builder;

    private static BufferedWriter itemWriter;
    private static BufferedWriter userWriter;
    private static BufferedWriter categoryWriter;
    private static BufferedWriter bidWriter;
    
    static final String[] typeName = {
	"none",
	"Element",
	"Attr",
	"Text",
	"CDATA",
	"EntityRef",
	"Entity",
	"ProcInstr",
	"Comment",
	"Document",
	"DocType",
	"DocFragment",
	"Notation",
    };
    
    static class MyErrorHandler implements ErrorHandler {
        
        public void warning(SAXParseException exception)
        throws SAXException {
            fatalError(exception);
        }
        
        public void error(SAXParseException exception)
        throws SAXException {
            fatalError(exception);
        }
        
        public void fatalError(SAXParseException exception)
        throws SAXException {
            exception.printStackTrace();
            System.out.println("There should be no errors " +
                               "in the supplied XML files.");
            System.exit(3);
        }
        
    }
    
    /* Non-recursive (NR) version of Node.getElementsByTagName(...)
     */
    static Element[] getElementsByTagNameNR(Element e, String tagName) {
        Vector< Element > elements = new Vector< Element >();
        Node child = e.getFirstChild();
        while (child != null) {
            if (child instanceof Element && child.getNodeName().equals(tagName))
            {
                elements.add( (Element)child );
            }
            child = child.getNextSibling();
        }
        Element[] result = new Element[elements.size()];
        elements.copyInto(result);
        return result;
    }
    
    /* Returns the first subelement of e matching the given tagName, or
     * null if one does not exist. NR means Non-Recursive.
     */
    static Element getElementByTagNameNR(Element e, String tagName) {
        Node child = e.getFirstChild();
        while (child != null) {
            if (child instanceof Element && child.getNodeName().equals(tagName))
                return (Element) child;
            child = child.getNextSibling();
        }
        return null;
    }
    
    /* Returns the text associated with the given element (which must have
     * type #PCDATA) as child, or "" if it contains no text.
     */
    static String getElementText(Element e) {
        if (e.getChildNodes().getLength() == 1) {
            Text elementText = (Text) e.getFirstChild();
            return elementText.getNodeValue();
        }
        else
            return "";
    }
    
    /* Returns the text (#PCDATA) associated with the first subelement X
     * of e with the given tagName. If no such X exists or X contains no
     * text, "" is returned. NR means Non-Recursive.
     */
    static String getElementTextByTagNameNR(Element e, String tagName) {
        Element elem = getElementByTagNameNR(e, tagName);
        if (elem != null)
            return getElementText(elem);
        else
            return "";
    }
    
    /* Returns the amount (in XXXXX.xx format) denoted by a money-string
     * like $3,453.23. Returns the input if the input is an empty string.
     */
    static String strip(String money) {
        if (money.equals(""))
            return money;
        else {
            double am = 0.0;
            NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
            try { am = nf.parse(money).doubleValue(); }
            catch (ParseException e) {
                System.out.println("This method should work for all " +
                                   "money values you find in our data.");
                System.exit(20);
            }
            nf.setGroupingUsed(false);
            return nf.format(am).substring(1);
        }
    }
    
    /* Converts date to yyyy-MM-dd format */

    static String convertDate(String s){

        String output = "";
        String format_old = "MMM-dd-yy HH:mm:ss";
        String format_new = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat date_transformer = new SimpleDateFormat(format_old);

        try{
            Date date = date_transformer.parse(s);
            date_transformer.applyPattern(format_new);
            output = date_transformer.format(date);

        }
        catch(ParseException pe){
            System.err.println("Problem formatting date");
        }

        return output;

    }

    /* Formats the strings into a data row by adding column seperators*/
    static String formatStrings(String... args){
        String new_row = "";
        int i = 0;
        for (; i < args.length - 1 ; i++){
            new_row += args[i] + columnSeparator;
        }
        new_row += args[i];
        return new_row;
    }


    /* Process one items-???.xml file.
     */
    static void processFile(File xmlFile) {
        Document doc = null;
        try {
            doc = builder.parse(xmlFile);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(3);
        }
        catch (SAXException e) {
            System.out.println("Parsing error on file " + xmlFile);
            System.out.println("  (not supposed to happen with supplied XML files)");
            e.printStackTrace();
            System.exit(3);
        }
        
        /* At this point 'doc' contains a DOM representation of an 'Items' XML
         * file. Use doc.getDocumentElement() to get the root Element. */
        System.out.println("Successfully parsed - " + xmlFile);
        
        /* Fill in code here (you will probably need to write auxiliary
            methods). */

        Element rootElement = doc.getDocumentElement();
        Element[] items = getElementsByTagNameNR(rootElement, "Item");


        try{

            /* Initiaitlize File Writers to write to relevant data files (one for each
            table) */
            itemWriter = new BufferedWriter(new FileWriter("item.dat",true));
            userWriter = new BufferedWriter(new FileWriter("user.dat",true));
            categoryWriter = new BufferedWriter(new FileWriter("category.dat",true));
            bidWriter = new BufferedWriter(new FileWriter("bid.dat",true));

            /* Parse every item belonging to the file */
            for(int i = 0; i < items.length; i++ ){
                parseItem(items[i]);
            }

            /* Close File Writers*/ 
            itemWriter.close();
            userWriter.close();
            categoryWriter.close();
            bidWriter.close();

        }
        catch(IOException e){
            e.printStackTrace();
        }
        
        
        
        /**************************************************************/
        
    }

    public static void parseItem(Element item) throws IOException { 

        /* ----------------*/
        /* ITEMS 
        /* ----------------*/

        /* Parse all relevant Item data */

        String item_id = item.getAttribute("ItemID");
        String name = getElementTextByTagNameNR(item, "Name");
        String buy_price = strip(getElementTextByTagNameNR(item,"Buy_Price"));
        String first_bid = strip(getElementTextByTagNameNR(item,"First_Price"));
        String currently = strip(getElementTextByTagNameNR(item,"Currently"));
        String number_of_bids = getElementTextByTagNameNR(item,"Number_of_Bids");
        String started =  "" +  convertDate(getElementTextByTagNameNR(item,"Started"));
        String ends =  "" +  convertDate(getElementTextByTagNameNR(item,"Ends"));
        String desc = getElementTextByTagNameNR(item, "Description");
        if (desc.length() > 4000)
                    desc = desc.substring(0, 4000);

        Element user = getElementByTagNameNR(item, "Seller");
        String user_id = user.getAttribute("UserID");


         /* Write Out Item data */
        itemWriter.write(formatStrings(item_id, name, user_id, buy_price, first_bid, currently, number_of_bids, started, ends, desc));
        itemWriter.newLine();


        /* ----------------*/
        /* USER
        /* ----------------*/

        /* Parse all relevant User data */
        String rating = user.getAttribute("Rating");
        String location = getElementText(getElementByTagNameNR(item, "Location"));
        String country = getElementText(getElementByTagNameNR(item, "Country"));

         /* Write Out User data */
        userWriter.write(formatStrings(user_id, rating, location, country));
        userWriter.newLine();


        /* ----------------*/
        /* CATERGORIES
        /* ----------------*/

        /* Extract all categories */
        Element[] categories = getElementsByTagNameNR(item, "Category");
        int category_count = categories.length;

        for (int i = 0; i < category_count; i++){
            String category = getElementText(categories[i]);
            /* Write Out Category data */
            categoryWriter.write(formatStrings(item_id, category));
            categoryWriter.newLine();
        }


        /* ----------------*/
        /* BID
        /* ----------------*/

        /* Extract all bids*/
        Element bid_wrapper = getElementByTagNameNR(item, "Bids");
        Element[] bids =  getElementsByTagNameNR(bid_wrapper, "Bid");

        int bid_count = bids.length;

        for (int i = 0; i < bid_count; i++){
            Element bidder = getElementByTagNameNR(bids[i], "Bidder");
            String bidder_id = bidder.getAttribute("UserID");
            String time = "" + convertDate(getElementTextByTagNameNR(bids[i], "Time"));
            String amount = strip(getElementTextByTagNameNR(bids[i], "Amount"));

            /* Write Out Bid data */
            bidWriter.write(formatStrings(bidder_id, item_id, time, amount));
            bidWriter.newLine();
        }

    }

    
    public static void main (String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java MyParser [file] [file] ...");
            System.exit(1);
        }
        
        /* Initialize parser. */
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringElementContentWhitespace(true);      
            builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new MyErrorHandler());
        }
        catch (FactoryConfigurationError e) {
            System.out.println("unable to get a document builder factory");
            System.exit(2);
        } 
        catch (ParserConfigurationException e) {
            System.out.println("parser was unable to be configured");
            System.exit(2);
        }

        /* Process all files listed on command line. */
        for (int i = 0; i < args.length; i++) {
            File currentFile = new File(args[i]);
            processFile(currentFile);
        }

    }
}
