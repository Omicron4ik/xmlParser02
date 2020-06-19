package ru.radionov;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;

public class streletsConfigParser {

    private static ArrayList<Device> deviceList = new ArrayList<>();
    private static ArrayList<Device> validDeviceList = new ArrayList<>();
    private static ArrayList<Device> inValidDeviceList = new ArrayList<>();
    static int n = 0;

    public static void main(String[] args) throws ParserConfigurationException, SAXException, Exception {

        properties properties = new properties();
        properties.loadConfig();

        int limit = Integer.parseInt(properties.getLimit());
        String url = "";
        String geocoded = "";

        //parsing config
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        SAXParser parser = saxParserFactory.newSAXParser();
        XMLConfigHandler configHandler = new XMLConfigHandler();
        parser.parse(new File(properties.getConfigFile()), configHandler);

        //geocoding addresses
        HttpConnection httpConnection = new HttpConnection();
        for (Device device: deviceList) {
            if (device.getAddress() != null) {
                url = properties.getBaseUrl() + "?apikey=" + properties.getApiKey() + "&geocode=" + URLEncoder.encode(device.getAddress());
                geocoded = httpConnection.sendGet(url);

                File tmpFile = File.createTempFile("geo", null);
                Path geoFile = Paths.get(tmpFile.getPath());
                Files.write(geoFile, geocoded.getBytes(), StandardOpenOption.APPEND);

                XMLGeocodingHanler geoHandler = new XMLGeocodingHanler(device.getAddress());
                parser.parse(tmpFile, geoHandler);
                device.setLon(geoHandler.lon);
                device.setLat(geoHandler.lat);
                device.setPrecision(geoHandler.precision);
            }
            n++;
            // limiter for yandex.api
            if (limit != -1 && n >= limit) {
                System.out.println("Limit requests is " + limit + ". Process stopped. Set limit to -1 in config.properties to encode all");
                break;
            }
        }
        for (Device device : deviceList) {
            if (device.getPrecision() != null && device.getPrecision().equals("exact")) {
                validDeviceList.add(device);
            } else {
                inValidDeviceList.add(device);
            }
        }
        mdmCreating.fillmdm(validDeviceList,properties.getPositiveResultFile());
        mdmCreating.fillmdm(inValidDeviceList,properties.getNevativeResultFile());
    }

    public static class HttpConnection {
        private final String userAgent = "Mozilla/5.0";

        private String sendGet (String url) throws Exception {
            URL obj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) obj.openConnection();

            //request method
            connection.setRequestMethod("GET");
            //request header
            connection.setRequestProperty("User-Agent",userAgent);

            int responseCode = connection.getResponseCode();

            System.out.println("Sending 'GET' request to URL: " + url);
            System.out.println("Response code:  " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            return response.toString();
        }
    }

    public static class XMLGeocodingHanler extends DefaultHandler {
        private String address,precision,coords,lon,lat,element;

        public XMLGeocodingHanler (String address) {
            this.address = address;
        }

        @Override
        public void startDocument() throws SAXException {
            //System.out.println("Start reading geoData...");
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            element = qName;
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (element.equals("precision")) {
                precision = new String(ch, start, length);
            } else if (element.equals("pos")) {
                coords = new String(ch, start, length);
                lon =  coords.split(" ")[0];
                lat =  coords.split(" ")[1];
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

        }

        @Override
        public void endDocument() throws SAXException {
            //System.out.println("geoData pasred!");
        }
    }

    public static class XMLConfigHandler extends DefaultHandler {

        private String curElement,deviceId,deviceAddress,deviceName;
        private Boolean thatObject;

        @Override
        public void startDocument() throws SAXException {
            System.out.println("Start reading config...");
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            curElement = qName;
            if (curElement != null && curElement.equals("GuardObject")) {
                deviceId = attributes.getValue("Id");
                thatObject = true;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (curElement != null) {
                if (curElement.equals("Name") && thatObject) {
                    deviceName = new String(ch, start, length);
                } else if (curElement.equals("Description") && thatObject) {
                    deviceAddress = new String(ch, start, length);
                    int AddrStart = deviceAddress.indexOf("<Address>");
                    int AddrFinish = deviceAddress.indexOf("</Address>");
                    if (AddrStart != -1) {
                        deviceAddress = deviceAddress.substring(AddrStart + 9, AddrFinish);
                    } else {
                        System.out.println("No address for id " + deviceId);
                        deviceAddress = null;
                    }
                    // Fix and reset device info
                    deviceList.add(new Device(deviceId, deviceAddress,deviceName));
                    deviceName = null;
                    deviceId = null;
                    deviceAddress = null;
                    thatObject = false;
                }
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            curElement = null;
        }

        @Override
        public void endDocument() throws SAXException {
            System.out.println("Config pasred!");
        }
    }

}
