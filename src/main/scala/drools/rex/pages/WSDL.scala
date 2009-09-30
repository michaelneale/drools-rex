package drools.rex.pages
/**
 * 
 * @author Michael Neale
 */

object WSDL {
  def getWSDL = {
    <definitions name="KnowledgeSession"
        targetNamespace="http://drools.org/drools-5.0/knowledge-session"
        xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"
        xmlns:xs="http://www.w3.org/2001/XMLSchema"
        xmlns:ks="http://drools.org/drools-5.0/knowledge-session"
        xmlns="http://schemas.xmlsoap.org/wsdl/">

        <types>
            <schema targetNamespace="http://drools.org/drools-5.0/knowledge-session" xmlns="http://www.w3.org/1999/XMLSchema">
                <xs:element name="batch-execution">
                   <xs:complexType>
                       <xs:sequence>
                           <!-- this is still being clarified in XSD... -->
                           <xs:element name="insert" minOccurs="0" maxOccurs="unbounded" type="xs:any"/>
                           <!-- facts are inserted in the form <org.acme.Something><fieldName>fieldValue</fieldName></org.acme.Something> -->
                           <xs:element name="insert-elements" minOccurs="0" maxOccurs="unbounded" type="xs:any"/>
                           <xs:element name="set-global" minOccurs="0" maxOccurs="unbounded" type="xs:any"/>
                           <xs:element name="query" minOccurs="0" maxOccurs="unbounded" type="xs:any"/>
                           <xs:element name="file-all-rules" minOccurs="0" maxOccurs="unbounded" type="xs:any"/>
                           <xs:element name="complete-work-item" minOccurs="0" maxOccurs="unbounded" type="xs:any"/>
                           <xs:element name="signal-event" minOccurs="0" maxOccurs="unbounded" type="xs:any"/>
                           <xs:element name="start-process" minOccurs="0" maxOccurs="unbounded" type="xs:any"/>
                           <xs:element name="get-global" minOccurs="0" maxOccurs="unbounded" type="xs:any"/>
                           <xs:element name="abort-work-item" minOccurs="0" maxOccurs="unbounded" type="xs:any"/>
                           <xs:any minOccurs="0"/>
                       </xs:sequence>
                   </xs:complexType>
                </xs:element>
                <xs:element name="execution-results">
                    <xs:complexType>
                        <xs:sequence>
                            <!-- this is still being clarified in XSD -->
                            <xs:element name="result" type="xs:any"/>
                            <xs:element name="fact-handle" type="xs:any"/>
                            <xs:element name="fact-handles" type="xs:any"/>
                            <xs:element name="query-results" type="xs:any"/>
                            <xs:element name="list" type="xs:any"/>
                            <xs:any minOccurs="0"/>
                        </xs:sequence>
                    </xs:complexType>
                </xs:element>

            </schema>
        </types>

        <message name="KBRequestIn">
            <part name="body" element="ks:KnowledgeBaseRequest"/>
        </message>

        <message name="KBResponseOut">
            <part name="body" element="ks:KnowledgeBaseResponse"/>
        </message>

        <portType name="KBSessionPortType">
            <operation name="KBSession">
                <input message="ks:KBRequestIn"/>
                <output message="ks:KBResponseOut"/>
            </operation>
        </portType>


        <binding name="KnowledgeSessionSoapBinding" type="ks:KBSessionPortType">
            <soap:binding style="document"
                transport="http://schemas.xmlsoap.org/soap/http"/>
            <operation name="KnowledgeSession">
                <soap:operation soapAction="http://drools.org/drools-5.0/KnowledgeSession"/>
                <input>
                    <soap:body use="literal" />
                </input>
                <output>
                    <soap:body use="literal" />
                </output>
            </operation>
        </binding>


        <service name="KnowledgeSessionService">
            <documentation>Drools Knowledge Session Service</documentation>
            <port name="KnowledgeSessionPort"
                binding="ks:KnowledgeSessionSoapBinding">
            </port>
        </service>

    </definitions>.toString









   

  }

}