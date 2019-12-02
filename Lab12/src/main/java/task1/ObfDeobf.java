package task1;


import lombok.Getter;
import lombok.Setter;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

public class ObfDeobf {

    @Setter
    @Getter
    static class Employee {
        private String id;
        private String firstName;
        private String lastName;
        private String location;

        @Override
        public String toString() {
            return "Employee:: ID=" + id + "; Name=" + firstName + " " + lastName + "; Location=" + location;
        }
    }

    private static List<Employee> readXML(String fileName) throws FileNotFoundException, XMLStreamException {
        File file = new File(fileName);

        XMLInputFactory factory = XMLInputFactory.newInstance();

        XMLEventReader eventReader = factory.createXMLEventReader(new FileReader(file));

        List<Employee> employeeList = new ArrayList<>();

        Employee employee = null;

        while (eventReader.hasNext()) {
            XMLEvent xmlEvent = eventReader.nextEvent();

            if (xmlEvent.isStartElement()) {
                StartElement startElement = xmlEvent.asStartElement();

                if ("employee".equalsIgnoreCase(startElement.getName().getLocalPart())) {
                    employee = new Employee();
                }

                Iterator<Attribute> iterator = startElement.getAttributes();

                while (iterator.hasNext()) {
                    Attribute attribute = iterator.next();
                    QName name = attribute.getName();
                    if ("id".equalsIgnoreCase(name.getLocalPart())) {
                        employee.setId(attribute.getValue());
                    }
                }

                switch (startElement.getName().getLocalPart()) {
                    case "firstName":
                        Characters fnameDataEvent = (Characters) eventReader.nextEvent();
                        employee.setFirstName(fnameDataEvent.getData());
                        break;

                    case "lastName":
                        Characters lnameDataEvent = (Characters) eventReader.nextEvent();
                        employee.setLastName(lnameDataEvent.getData());
                        break;
                    case "location":
                        Characters locationDataEvent = (Characters) eventReader.nextEvent();
                        employee.setLocation(locationDataEvent.getData());
                        break;
                }
            }

            if (xmlEvent.isEndElement()) {
                EndElement endElement = xmlEvent.asEndElement();

                if ("employee".equalsIgnoreCase(endElement.getName().getLocalPart())) {
                    employeeList.add(employee);
                }
            }
        }
        return employeeList;
    }

    private static void createNode(XMLStreamWriter writer, Employee employee) throws XMLStreamException {
        writer.writeStartElement("employee");
        writer.writeAttribute("id",employee.getId());

        writer.writeCharacters(System.getProperty("line.separator"));
        writer.writeStartElement("firstName");
        writer.writeCharacters(employee.getFirstName());
        writer.writeEndElement();
        writer.writeCharacters(System.getProperty("line.separator"));
        writer.writeStartElement("lastName");
        writer.writeCharacters(employee.getLastName());
        writer.writeEndElement();
        writer.writeCharacters(System.getProperty("line.separator"));
        writer.writeStartElement("location");
        writer.writeCharacters(employee.getLocation());
        writer.writeEndElement();
        writer.writeCharacters(System.getProperty("line.separator"));

        writer.writeEndElement();
    }


    private static void writeXML(String fileName, List<Employee> list) {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();

        try {
            XMLStreamWriter writer = factory.createXMLStreamWriter(
                    new FileWriter(fileName));

            writer.writeStartDocument();
            writer.writeCharacters(System.getProperty("line.separator"));
            writer.writeStartElement("employees");
            writer.writeCharacters(System.getProperty("line.separator"));


            for (Employee e : list){
                createNode(writer,e);
                writer.writeCharacters(System.getProperty("line.separator"));
            }

            writer.writeEndElement();
            writer.writeEndDocument();

            writer.flush();
            writer.close();

        } catch (XMLStreamException | IOException e) {
            e.printStackTrace();
        }
    }

    private static String source = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static String target = "Q5A8ZWS0XEDC6RFVT9GBY4HNU3J2MI1KO7LP";

    private static String obfuscateString(String s) {
        StringBuilder r = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            r.append(target.charAt(source.indexOf(s.toUpperCase().charAt(i))));
        }
        return r.toString();
    }

    private static void processEmployee(Employee e, Function<String, String> f) {
        e.setId(f.apply(e.getId()));
        e.setFirstName(f.apply(e.getFirstName()));
        e.setLastName(f.apply(e.getLastName()));
        e.setLocation(f.apply(e.getLocation()));
    }

    private static String unobfuscateString(String s) {
        StringBuilder r = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            r.append(source.charAt(target.indexOf(s.toUpperCase().charAt(i))));
        }
        return r.toString();
    }


    public static void main(String[] args) throws FileNotFoundException, XMLStreamException {
        String input = "src/main/resources/example.xml";
        String output = "src/main/resources/out.xml";
        String obfus = "src/main/resources/ubf.xml";
        List<Employee> list = readXML(input);
        for (Employee e : list) {
            processEmployee(e, ObfDeobf::obfuscateString);
        }
        System.out.println(list);

        writeXML(obfus, list);

        for (Employee e : list) {
            processEmployee(e, ObfDeobf::unobfuscateString);
        }
        System.out.println(list);

        writeXML(output, list);
    }
}
