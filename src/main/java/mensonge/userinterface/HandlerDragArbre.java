package mensonge.userinterface;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import mensonge.core.BaseDeDonnees.BaseDeDonnees;
import mensonge.core.BaseDeDonnees.DBException;

/**
 * Classe permettant d'effectuer le drag and drop sur l'arbre
 * 
 * @author Azazel
 * 
 */
public class HandlerDragArbre extends TransferHandler
{
	private static final long serialVersionUID = 1L;
	private BaseDeDonnees bdd;
	private PanneauArbre panneauArbre;
	private static Logger logger = Logger.getLogger("drag Arbre");

	/**
	 * Constructeur par defaut
	 * 
	 * @param panneauArbre
	 *            pour connaitre le tri actuel
	 * @param bdd
	 *            la base pour mettre à jour
	 */
	public HandlerDragArbre(PanneauArbre panneauArbre, BaseDeDonnees bdd)
	{
		this.bdd = bdd;
		this.panneauArbre = panneauArbre;
	}

	/**
	 * Méthode permettant à l'objet de savoir si les données reçues via un drop sont autorisées à être importées
	 * 
	 * @param info
	 * @return boolean
	 */
	public boolean canImport(TransferHandler.TransferSupport info)
	{
		if (!info.isDataFlavorSupported(DataFlavor.stringFlavor))
		{
			return false;
		}
		return true;
	}

	/**
	 * C'est ici que l'insertion des données dans notre composant est réalisée
	 * 
	 * @param support
	 * @return boolean
	 */
	public boolean importData(TransferHandler.TransferSupport support)
	{
		// On récupère l'endroit du drop via un objet approprié
		JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
		// Les informations afin de pouvoir créer un nouvel élément
		TreePath path = dl.getPath();
		// On verifie que le chemin du drop porte bien sur une branche
		if (!(path.getLastPathComponent() instanceof Branche))
		{
			return false;
		}
		Branche cat = (Branche) path.getLastPathComponent();
		// On verifie bien que l'userObject est une chaine utilisable
		if (!(cat.getUserObject() instanceof String))
		{
			return false;
		}
		Transferable data = support.getTransferable();// On recupere le transferable
		String str = "", nom = (String) cat.getUserObject();// On recupere le nom de la nouvelle categorie en fonction
															// du chemin
		try
		{
			str = (String) data.getTransferData(DataFlavor.stringFlavor);// On extrait la chaine du transferable
		}
		catch (Exception e)
		{
			logger.log(Level.WARNING, e.getLocalizedMessage());
		}
		// Si la chaine est vide ou correspondant a -1 (une erreur de selection) on stop
		if (str.equals("") || str.equals("-1"))
		{
			return false;
		}
		int[] listeId = this.convertirListeId(str.split(";"));

		for (int id : listeId)
		{
			// On modifie les categories ou le sujet
			effectuerMaj(id, nom);
		}
		return true;
	}

	/**
	 * Convertie un tableau de string contenant des nombres en tableau de in
	 * 
	 * @param tab
	 * @return
	 */
	private static int[] convertirListeId(String[] tab)
	{
		int[] retour = new int[tab.length];
		int i = 0;
		for (String nombre : tab)
		{
			try
			// S'il y a un erreur de conversion, on met -1
			{
				retour[i] = Integer.parseInt(nombre);
			}
			catch (NumberFormatException e)
			{
				logger.log(Level.WARNING, e.getLocalizedMessage());
				retour[i] = -1;
			}
			i++;
		}
		return retour;
	}

	private void effectuerMaj(int id, String nom)
	{
		try
		{
			if (this.panneauArbre.getTypeTrie() == PanneauArbre.TYPE_TRIE_CATEGORIE)
			{
				this.bdd.modifierEnregistrementCategorie(id, this.bdd.getCategorie(nom));
			}
			else
			{
				this.bdd.modifierEnregistrementSujet(id, this.bdd.getSujet(nom));
			}
		}
		catch (DBException e)
		{
			logger.log(Level.WARNING, e.getLocalizedMessage());
		}
	}

	/**
	 * Dans cette méthode, nous allons créer l'objet utilisé par le système de drag'n drop afin de faire circuler les
	 * données entre les composants Vous pouvez voir qu'il s'agit d'un objet de type Transferable
	 * 
	 * @param c
	 * @return
	 */
	protected Transferable createTransferable(JComponent c)
	{
		// On retourne un nouvel objet implémentant l'interface Transferable
		// StringSelection implémente cette interface, nous l'utilisons donc
		JTree arbre = (JTree) c;// on convertie le compansant en JTree
		Feuille feuille;
		String chaine = "";

		for (TreePath path : arbre.getSelectionPaths())
		{
			if (path.getLastPathComponent() instanceof Feuille)// On recupére l'id de toutes les feuilles
			{
				feuille = (Feuille) path.getLastPathComponent();
				chaine += feuille.getId() + ";";
			}
		}
		return new StringSelection(chaine);
	}

	public int getSourceActions(JComponent c)
	{
		return MOVE;
	}
}