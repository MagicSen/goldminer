package miner.main;

import miner.IGoldMiner;
import miner.IGoldMinerImpl;
import miner.ontology.Ontology;
import miner.ontology.ParsedAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;

public class ParseRulesAndCreateOntology {

	private static IGoldMiner goldMiner;

	public static void main(String[] args) {
		try {
			goldMiner = new IGoldMinerImpl();
			HashMap<OWLAxiom,ParsedAxiom.SupportConfidenceTuple> axioms = goldMiner.parseAssociationRules();
			System.out.println("Anzahl Axiome: " + axioms.size());
			Ontology o = goldMiner.createOntology(axioms, 0.0, 0.0);
			//o = goldMiner.greedyDebug(o);
            System.out.println("Saving ontology");
			o.save();
            System.out.println("Done saving");
			goldMiner.disconnect();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
