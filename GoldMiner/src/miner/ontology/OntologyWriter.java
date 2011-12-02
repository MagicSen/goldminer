package miner.ontology;

import miner.database.Database;
import miner.database.SQLFactory;
import miner.util.Settings;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class OntologyWriter {

	private Ontology m_ontology;

	private Database m_database;

	private SQLFactory m_sqlFactory;
    private HashMap<Integer, String> classURICache;
    private HashMap<Integer, String> propertyURICache;
    private HashMap<Integer, String> disjointPropertyURICache;
    private HashMap<Integer, String> symmetryPropertyURICache;
    private HashMap<Integer, String> propertyURIFromExistsCache;
    private HashMap<Integer, String> classURIFromExistsCache;
    private HashMap<Integer, String> propertyURIFromExistsPropertyTopCache;


    public OntologyWriter(Database d, Ontology o) throws SQLException {
		m_ontology = o;
		m_sqlFactory = new SQLFactory();
		m_database = d;

        // cache ID to URI maps
        classURICache = new HashMap<Integer, String>();
        propertyURICache = new HashMap<Integer, String>();
        disjointPropertyURICache = new HashMap<Integer, String>();
        symmetryPropertyURICache = new HashMap<Integer, String>();
        propertyURIFromExistsCache = new HashMap<Integer, String>();
        classURIFromExistsCache = new HashMap<Integer, String>();
        propertyURIFromExistsPropertyTopCache = new HashMap<Integer, String>();

        ResultSet res = m_database.query(m_sqlFactory.selectClassURIQuery());

        while (res.next()) {
            classURICache.put(res.getInt(1), res.getString(2));
        }

        res.getStatement().close();

        res = m_database.query(m_sqlFactory.selectPropertiesQuery());

        while (res.next()) {
            String uri = res.getString("uri");
            int id = res.getInt("id");
            int disjId = res.getInt("disjointID");
            int symId = res.getInt("symmetryId");

            propertyURICache.put(id, uri);
            disjointPropertyURICache.put(disjId, uri);
            symmetryPropertyURICache.put(symId, uri);
        }
        res.getStatement().close();
        
        res = m_database.query(m_sqlFactory.selectURIsFromExistsQuery());

        while (res.next()) {
            int id = res.getInt("id");
            String propUri = res.getString("prop_uri");
            String classUri = res.getString("class_uri");

            propertyURIFromExistsCache.put(id, propUri);
            classURIFromExistsCache.put(id, classUri);
        }
        res.getStatement().close();

        res = m_database.query(m_sqlFactory.selectURIsFromExistsTopQuery());

        while (res.next()) {
            int id = res.getInt("id");
            String uri = res.getString("uri");

            propertyURIFromExistsPropertyTopCache.put(id, uri);
        }
        res.getStatement().close();
	}

    public Ontology write(HashMap<OWLAxiom, ParsedAxiom.SupportConfidenceTuple> axioms, double supportThreshold, double confidenceThreshold)
        throws OWLOntologyStorageException {

        //List<OWLAxiom> axioms = getAxioms();
        //System.out.println( "axioms: "+ axioms.size() );
        int i = 0;
        for (Map.Entry<OWLAxiom, ParsedAxiom.SupportConfidenceTuple> entry : axioms.entrySet()) {
            if (entry.getValue().getSupport() > supportThreshold &&
                entry.getValue().getConfidence() > confidenceThreshold) {

                //System.out.println("add (" + i + "): " + entry);
                m_ontology.addAxiom(entry.getKey());
            }
            i++;
        }
        return this.m_ontology;
    }

	private List<OWLAxiom> getAxioms() throws Exception {
		HashMap<OWLAxiom,Double> hmAxioms = new HashMap<OWLAxiom,Double>();
		//hmAxioms.putAll( get_c_sub_c_Axioms() );
		//hmAxioms.putAll( get_c_and_c_sub_c_Axioms() );
		//hmAxioms.putAll( get_c_dis_c_Axioms() );
		//hmAxioms.putAll( get_exists_p_c_sub_c_Axioms() );
		//hmAxioms.putAll( get_c_sub_exists_p_c_Axioms() );
		//hmAxioms.putAll( get_p_sub_p_Axioms() );
		//hmAxioms.putAll( get_exists_p_T_sub_c_Axioms() );
		//hmAxioms.putAll( get_exists_pi_T_sub_c_Axioms() );

		//Collections.sort(hmAxioms);
		return sort( hmAxioms );
	}

	public OWLAxiom get_c_sub_c_Axioms(int iCons, int iAnte, double dSupp, double conf) throws SQLException {
		// assumption: only one confidence value per axiom
		OWLAxiom axiom = m_ontology.get_c_sub_c_Axiom( getClassURI( iAnte ), getClassURI( iCons ), dSupp, conf );
		return axiom;
	}

	 public OWLAxiom get_c_dis_c_Axioms(int iAnte, int iCons, double supp, double conf) throws SQLException {
			// assumption: only one confidence value per axiom
			OWLAxiom axiom = m_ontology.get_c_dis_c_Axiom( getClassURI( iAnte ), getClassURI( iCons ), supp, conf );
			return axiom;
		}

	 public OWLAxiom get_p_sub_p_Axioms(int iAnte, int iCons, double supp, double conf) throws SQLException {
			// assumption: only one confidence value per axiom
			OWLAxiom axiom = m_ontology.get_p_sub_p_Axiom( getPropertyURI( iAnte ), getPropertyURI( iCons ), supp, conf );
			return axiom;
		}

	 public OWLAxiom get_p_dis_p_Axioms(int iAnte, int iCons, double supp, double conf) throws SQLException {
		 OWLAxiom axiom = m_ontology.get_p_dis_p_Axiom(getPropertyURI( iAnte ), getDisjointPropertyURI( iCons ), supp, conf);
		 return axiom;
	 }

	 public OWLAxiom get_p_reflexive_Axioms(int iAnte, int iCons, double supp, double conf) throws SQLException {
		 OWLAxiom axiom = m_ontology.getPropertyReflexivityAxiom(iAnte + "", getPropertyURI(iCons), supp, conf);
		 return axiom;
	 }

	 public OWLAxiom get_p_irreflexive_Axioms(int iAnte, int iCons, double supp, double conf) throws SQLException {
		 OWLAxiom axiom = m_ontology.getPropertyIrreflexivityAxiom(iAnte + "", getDisjointPropertyURI(iCons), supp, conf);
		 return axiom;
	 }

	 public OWLAxiom get_c_and_c_sub_c_Axioms(int iAnte1, int iAnte2, int iCons, double supp, double conf) throws SQLException {
			// assumption: only one confidence value per axiom
			OWLAxiom axiom = m_ontology.get_c_and_c_sub_c_Axiom( getClassURI( iAnte1 ), getClassURI(iAnte2), getClassURI( iCons ), supp, conf );
			return axiom;
	}

	 public OWLAxiom get_exists_p_c_sub_c_Axioms(int iPropExists, int iCons, double supp, double conf) throws SQLException {
			// assumption: only one confidence value per axiom
			OWLAxiom axiom = m_ontology.get_exists_p_c_sub_c_Axiom( getPropertyURIFromExistsProperty(iPropExists), getClassURIFromExistsProperty(iPropExists), getClassURI( iCons ), supp, conf );
			return axiom;
	}

	 public OWLAxiom get_exists_p_T_sub_c_Axioms(int iPropExists, int iCons, double supp, double conf) throws SQLException {
			// assumption: only one confidence value per axiom
			OWLAxiom axiom = m_ontology.get_exists_p_T_sub_c_Axiom( getPropertyURIFromExistsPropertyTop(iPropExists), getClassURI( iCons ), supp, conf );
			return axiom;
	}

	 public OWLAxiom get_exists_pi_T_sub_c_Axioms(int iPropExists, int iCons, double supp, double conf) throws SQLException {
			// assumption: only one confidence value per axiom
			OWLAxiom axiom = m_ontology.get_exists_pi_T_sub_c_Axiom( getPropertyURIFromExistsPropertyTop(iPropExists), getClassURI( iCons ), supp, conf );
			return axiom;
	}

	 public OWLAxiom get_c_sub_exists_p_c_Axioms(int iAnte, int iPropExists, double supp, double conf) throws SQLException {
			// assumption: only one confidence value per axiom
			OWLAxiom axiom = m_ontology.get_c_sub_exists_p_c_Axiom(getClassURI( iAnte ), getPropertyURIFromExistsProperty(iPropExists), getClassURIFromExistsProperty(iPropExists), supp, conf);
			return axiom;
	}
	 public OWLAxiom get_p_inverse_q_Axioms(int iAnte, int iCons, double supp, double conf) throws SQLException {
		 	OWLAxiom axiom = m_ontology.getPropertyInverseAxiom(this.getPropertyURI(iAnte), this.getInversePropertyURI(iCons), supp, conf);
		 	return axiom;
	 }

	 public OWLAxiom get_p_asymmetric_Axioms(int iAnte, int iCons, double supp, double conf) throws SQLException {
		 	OWLAxiom axiom = m_ontology.getPropertyAsymmetricAxiom(this.getPropertyURI(iAnte), this.getSymmetryPropertyURI( iCons ), supp, conf);
		 	return axiom;
	 }

	 public OWLAxiom get_p_functional_Axioms(int iAnte, int iCons, double supp, double conf) throws SQLException {
		 OWLAxiom axiom = m_ontology.getPropertyFunctionalAxiom(getPropertyURI(iAnte), getDisjointPropertyURI(iCons), supp, conf);
		 return axiom;
	 }

	 public OWLAxiom get_p_inverse_functional_Axioms(int iAnte, int iCons, double supp, double conf) throws SQLException {
		 OWLAxiom axiom = m_ontology.getPropertyInverseFunctionalAxiom(getPropertyURI(iAnte), getSymmetryPropertyURI(iCons), supp, conf);
		 return axiom;
	 }

	 public OWLAxiom get_p_chain_p_sub_p_Axioms(int iAnte, int iCons, double supp, double conf) throws SQLException {
		 String sQuery = m_sqlFactory.selectURIFromPropertyChainsTrans(iAnte);
		 ResultSet results = m_database.query(sQuery);
		 String uri = null;
		 if(results.next()) {
			 uri = results.getString("uri");
		 }
         results.getStatement().close();
		 OWLAxiom axiom = m_ontology.get_p_chain_p_sub_p_Axiom(uri, getPropertyURI(iCons), supp, conf);
		 return axiom;
	 }

	 public OWLAxiom get_p_chain_q_sub_r_Axioms(int iAnte, int iCons, double supp, double conf) throws SQLException {
		String sQuery = m_sqlFactory.selectURIsFromPropertyChains(iAnte);
		ResultSet results = m_database.query(sQuery);
		String uri1;
		String uri2;
		List<String> uris = null;
		if(results.next()) {
			uri1 = results.getString("uri1");
			uri2 = results.getString("uri2");
			uris = new ArrayList<String>();
			uris.add(uri1);
			uris.add(uri2);
		}
        results.getStatement().close();
		OWLAxiom axiom = m_ontology.get_p_chain_q_sub_r_Axiom(uris, getPropertyURI(iCons), supp, conf);
		return axiom;
	 }

	private List<OWLAxiom> sort( HashMap<OWLAxiom,Double> hmAxioms ){
		List<OWLAxiom> axioms = new ArrayList<OWLAxiom>( hmAxioms.keySet() );
		Collections.sort( axioms, new AxiomComparator( hmAxioms ) );
		return axioms;
	}

	public class AxiomComparator implements Comparator<OWLAxiom>
	{
		private HashMap<OWLAxiom,Double> hmAxioms;

		public AxiomComparator( HashMap<OWLAxiom,Double> hmAxioms ){
			this.hmAxioms = hmAxioms;
		}
		// TODO: ascending or descending order
		public int compare( OWLAxiom axiom1, OWLAxiom axiom2 ){
			Double d1 = hmAxioms.get( axiom1 );
			Double d2 = hmAxioms.get( axiom2 );
			return Double.compare( d2, d1 );
		}
		public boolean equals( Object object ){
			return false;
		}
	}

	public String getClassURI( int iClassID ) throws SQLException {
        return classURICache.get(iClassID);
	}

	public String getPropertyURI( int iPropertyID ) throws SQLException {
        return propertyURICache.get(iPropertyID);
    }

	public String getDisjointPropertyURI( int iPropertyID ) throws SQLException {
        return disjointPropertyURICache.get(iPropertyID);
    }

	public String getInversePropertyURI( int iPropertyID ) throws SQLException {
        return disjointPropertyURICache.get(iPropertyID);
    }

	public String getSymmetryPropertyURI( int iPropertyID ) throws SQLException {
        return symmetryPropertyURICache.get(iPropertyID);
    }

	//retrieves the property ID form the property_exists table
	public String getPropertyURIFromExistsProperty( int iExistsPropertyID ) throws SQLException {
        return propertyURIFromExistsCache.get(iExistsPropertyID);
    }

	//retrieves the property ID form the property_exists table
	public String getClassURIFromExistsProperty( int iExistsPropertyID ) throws SQLException {
        return classURIFromExistsCache.get(iExistsPropertyID);
    }

	//retrieves the property ID form the property_exists_top table
	public String getPropertyURIFromExistsPropertyTop( int iExistsPropertyID ) throws SQLException {
        return propertyURIFromExistsPropertyTopCache.get(iExistsPropertyID);
    }

	public Ontology writeClassesAndPropertiesToOntology() throws SQLException, OWLOntologyStorageException {
		String query = m_sqlFactory.selectClassURIsQuery();
		ResultSet results = m_database.query(query);
		while(results.next()) {
			IRI iri = IRI.create(Settings.getString("ontology_logical") + "#" + results.getString("name"));
			this.m_ontology.addClass(iri);
		}
        results.getStatement().close();
		String query2 = m_sqlFactory.selectPropertyURIsQuery();
		ResultSet results2 = m_database.query(query2);
		while(results2.next()) {
			IRI iri = IRI.create(Settings.getString("ontology_logical") + "#" + results2.getString("name"));
			this.m_ontology.addProperty(iri);
		}
        results2.getStatement().close();
		return this.m_ontology;
	}
}
