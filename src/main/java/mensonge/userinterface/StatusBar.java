package mensonge.userinterface;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.RootPaneContainer;
import javax.swing.Timer;

import mensonge.core.tools.ActionMessageObserver;
import mensonge.core.tools.CacheObserver;
import mensonge.core.tools.DataBaseObserver;
import mensonge.core.tools.Utils;

/**
 * Barre de status de l'application. Elle écoute les différents observable : extraction, cache, bdd et affiche leurs messages
 *
 */
public class StatusBar extends JPanel implements ActionListener, ActionMessageObserver, DataBaseObserver, CacheObserver
{
	private static final long serialVersionUID = 8573623540967463794L;
	private static final int STATUS_BAR_HEIGHT = 16;
	private static final Cursor WAIT_CURSOR = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
	private static final Cursor DEFAULT_CURSOR = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
	private static final int PANEL_MARGIN = 10;
	private static final MouseAdapter MOUSE_ADAPTER = new MouseAdapter()
	{
	};
	/**
	 * Temps après lequel le message disparaitra, en millisecondes
	 */
	private static final int TIMER_DELAY = 10000;
	private Timer timer;
	private JLabel status;
	private JLabel dbSize;
	private JLabel cacheSize;

	/**
	 * Créé une nouvelle barre de status
	 */
	public StatusBar()
	{
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.status = new JLabel();
		this.add(status);

		this.cacheSize = new JLabel();
		this.dbSize = new JLabel("Taille de la base de données : "
				+ Utils.humanReadableByteCount(Utils.getDBSize(), false));
		this.add(Box.createHorizontalGlue());
		this.add(cacheSize);
		this.add(Box.createHorizontalStrut(PANEL_MARGIN));
		this.add(dbSize);

		setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.GRAY));
		setPreferredSize(new Dimension(this.getWidth(), STATUS_BAR_HEIGHT));
		timer = new Timer(TIMER_DELAY, this);
	}

	/**
	 * Défini le nouveau message à afficher dans la barre de status et stop le timer de disparition du message
	 * @param message Message à afficher
	 */
	public void setMessage(String message)
	{
		this.status.setText(" " + message);
		repaint();
		this.timer.stop();
	}

	/**
	 * Relance le timer de disparition du message
	 */
	public void done()
	{
		this.timer.restart();
	}

	@Override
	public void actionPerformed(ActionEvent arg0)
	{
		this.status.setText("");
		this.timer.stop();
	}

	@Override
	public void onInProgressAction(String message)
	{
		this.setMessage(message);
		RootPaneContainer root = ((RootPaneContainer) this.getTopLevelAncestor());
		root.getGlassPane().setCursor(WAIT_CURSOR);
		root.getGlassPane().addMouseListener(MOUSE_ADAPTER);
		root.getGlassPane().setVisible(true);
	}

	@Override
	public void onCompletedAction(String message)
	{
		this.setMessage(message);
		this.done();
		RootPaneContainer root = ((RootPaneContainer) this.getTopLevelAncestor());
		root.getGlassPane().addMouseListener(MOUSE_ADAPTER);
		root.getGlassPane().setCursor(DEFAULT_CURSOR);
		root.getGlassPane().setVisible(false);
	}

	@Override
	public void onUpdateCache(long newCacheSize)
	{
		cacheSize.setText("Taille du cache : " + Utils.humanReadableByteCount(newCacheSize, false));
	}

	@Override
	public void onUpdateDataBase()
	{
		dbSize.setText("Taille de la base de données : " + Utils.humanReadableByteCount(Utils.getDBSize(), false));
	}
}
