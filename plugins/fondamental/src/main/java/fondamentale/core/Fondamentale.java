package fondamentale.core;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JTable;

import mensonge.core.IExtraction;
import mensonge.core.database.DBException;
import mensonge.core.database.IBaseDeDonnees;
import mensonge.core.plugins.Plugin;

import fondamentale.userinterface.Fenetre;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class Fondamentale implements Plugin
{
	private static Logger logger = Logger.getLogger("Fondamentale");
	private boolean isActive = false;
	private static final int SIZE_BLOC = 10;
	private double retour[];

	private double[] drawGraph(final double[][] echantillons, final float sampleRate)
	{
		// 1/NB_SAMPLES = frequence
		double[] samplesFFT = new double[echantillons.length];
		this.retour = new double[echantillons[0].length];
		double[] hamming = hamming(echantillons.length);

		for (int j = 0; j < echantillons[0].length; j++)
		{
			for (int i = 0; i < echantillons.length; i++)
			{
				samplesFFT[i] = echantillons[i][j] * hamming[i];
			}
			final DoubleFFT_1D fft = new DoubleFFT_1D(samplesFFT.length);
			fft.realForward(samplesFFT);

			int index = indexBlocMax(SIZE_BLOC, samplesFFT);
			double tableauTmp[] = new double[SIZE_BLOC];
			System.arraycopy(samplesFFT, index, tableauTmp, 0, SIZE_BLOC);
			index += indexMax(tableauTmp);
			double fondamentale = sampleRate * (index / 2 - 1) / samplesFFT.length;
			retour[j] = fondamentale;
		}
		return this.retour;
	}

	private static double[] hamming(int length)
	{
		double[] window = new double[length];
		int m = length / 2;
		double r = Math.PI * 2 / length;
		for (int n = -m; n < m; n++)
		{
			window[m + n] = 0.54 + 0.46 * Math.cos(n * r);
		}
		return window;
	}

	@Override
	public void lancer(IExtraction extraction, Map<Integer, File> listeFichiersSelectionnes, IBaseDeDonnees bdd)
	{
		this.isActive = true;
		List<double[]> resultat = new LinkedList<double[]>();
		int nbColonne = 0;
		List<String> listeFile = new LinkedList<String>();
		if (!listeFichiersSelectionnes.isEmpty())
		{
			Set<Integer> keys = listeFichiersSelectionnes.keySet();

			for (Integer key : keys)
			{
				File file = listeFichiersSelectionnes.get(key);
				try
				{
					listeFile.add(bdd.getNomEnregistrement(key.intValue()));
				}
				catch (DBException e1)
				{
					e1.printStackTrace();
				}

				try
				{
					AudioInputStream inputAIS = AudioSystem.getAudioInputStream(file);
					AudioFormat audioFormat = inputAIS.getFormat();
					double[][] echantillons = extraction.extraireEchantillons(file.getCanonicalPath());
					double[] tabTmp = this.drawGraph(echantillons, audioFormat.getSampleRate());
					resultat.add(tabTmp);

					if (tabTmp.length > nbColonne)
					{
						nbColonne = tabTmp.length;
					}
				}
				catch (IOException e)
				{
					logger.log(Level.WARNING, e.getLocalizedMessage());
				}
				catch (UnsupportedAudioFileException e)
				{
					logger.log(Level.WARNING, e.getLocalizedMessage());
				}
			}
			JTable tableau = Fondamentale.creerTableau(resultat, listeFile, nbColonne);
			new Fenetre(tableau);
		}
		this.isActive = false;
	}

	@Override
	public void stopper()
	{
		this.isActive = false;
	}

	@Override
	public String getNom()
	{
		return "Fondamentale";
	}

	@Override
	public boolean isActive()
	{
		return isActive;
	}

	public static int indexBlocMax(int tailleBloc, double[] samples)
	{
		int retour = 0;
		double max = 0, tmp = 0;
		for (int i = 0; i < tailleBloc; i++)
		{
			max += samples[i];
		}

		for (int i = tailleBloc; i < samples.length; i += tailleBloc)
		{

			tmp = 0;
			for (int j = 0; j < tailleBloc && i + j < samples.length; j++)
			{
				if (i + j >= samples.length)
				{
					break;
				}
				tmp += samples[i + j];
			}
			if (tmp > max)
			{
				max = tmp;
				retour = i;
			}
		}
		return retour;
	}

	public static int indexMax(double[] samples)
	{
		int retour = 0;
		double max = samples[0];
		for (int i = 0; i < samples.length; i++)
		{
			if (samples[i] > max)
			{
				max = samples[i];
				retour = i;
			}
		}
		return retour;
	}

	public static JTable creerTableau(List<double[]> liste, List<String> fichier, int nbColonne)
	{
		String[] titre = Fondamentale.creerTitre(nbColonne);
		Object[][] data = new Object[fichier.size()][nbColonne + 1];
		int i = 0;

		for (double[] canauxFonda : liste)
		{
			data[i][0] = fichier.get(i);
			for (int j = 0; j < canauxFonda.length; j++)
			{
				data[i][j + 1] = canauxFonda[j];
			}
			i++;
		}
		return new JTable(data, titre);
	}

	public static String[] creerTitre(int nbColonne)
	{
		String[] retour = new String[nbColonne + 1];
		retour[0] = "Fichier";
		for (int i = 0; i < nbColonne; i++)
		{
			retour[i + 1] = "Canal " + (i + 1);
		}
		return retour;
	}
}
