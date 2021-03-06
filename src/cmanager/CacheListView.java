package cmanager;

import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;

import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import java.awt.Toolkit;

import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.awt.event.ActionEvent;
import javax.swing.JSplitPane;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.List;
import java.awt.Point;
import javax.swing.BoxLayout;

public class CacheListView extends JInternalFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3610178481183679565L;
	private CacheListController clc;
	private JTable table;
	private CachePanel panelCache;
	private JLabel lblCacheCount;
	private JLabel lblLblwaypointscount;
	private CustomJMapViewer mapViewer;
	private JPanel panelFilters;



	/**
	 * Create the frame.
	 */
	public CacheListView(final CacheListController clc) {
		this.clc = clc;
		
		AbstractTableModel atm = clc.getTableModel();
		table = new JTable(atm);
		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);	//column autosize
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) 
			{
				updateCachePanelToSelection();
				updateMapMarkers();
			}
		});
		
		panelFilters = new JPanel();
		panelFilters.setVisible(false);
		getContentPane().add(panelFilters, BorderLayout.NORTH);
		panelFilters.setLayout(new BoxLayout(panelFilters, BoxLayout.Y_AXIS));
		
		
		final JScrollPane scrollPane = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setMinimumSize(new Dimension(200, 300));

		
		final JSplitPane splitPane1 = new JSplitPane();
		getContentPane().add(splitPane1, BorderLayout.CENTER);
		splitPane1.setLeftComponent( scrollPane );
		
		
		final JSplitPane splitPane2 = new JSplitPane();
		splitPane1.setRightComponent(splitPane2);
		splitPane2.setVisible(false);
		
		panelCache = new CachePanel();
		panelCache.setVisible(false);
		splitPane2.setLeftComponent(panelCache);
		
		
		final JPanel panelMap = new JPanel();
		panelMap.setVisible(false);
		splitPane2.setRightComponent(panelMap);
		panelMap.setLayout(new BorderLayout(0, 0));
		
		
		
		mapViewer = new CustomJMapViewer();
		mapViewer.setFocusable(true);
		panelMap.add(mapViewer, BorderLayout.CENTER);
		
		JPanel panel_2 = new JPanel();
		panelMap.add(panel_2, BorderLayout.SOUTH);
		
		JLabel lblNewLabel = new JLabel("Drag map with right mouse, selection box with left mouse.");
		lblNewLabel.setFont(new Font("Dialog", Font.BOLD, 9));
		panel_2.add(lblNewLabel);

		// Make map movable with mouse
//		DefaultMapController mapController = new DefaultMapController(mapViewer);
//	    mapController.setMovementMouseButton(MouseEvent.BUTTON1);

	    mapViewer.addMouseListener(new MouseAdapter() 
	    {
	    	@Override
	    	public void mouseClicked(MouseEvent e) {
	    		super.mouseClicked(e);
	    		
                if(e.getButton() == MouseEvent.BUTTON1)
				{
					Point p = e.getPoint();
					Geocache g = getMapFocusedCache(p);
					if( g == null )
						return;
					
					if( e.getClickCount() == 1 && ((e.getModifiers() & InputEvent.CTRL_MASK) != 0 ) )
						Main.openUrl( g.getURL() );
					else if( e.getClickCount() == 1 )
						panelCache.setCache(g);
				}
	    	}	
		});
	    mapViewer.addMouseMotionListener(new MouseAdapter() 
	    {
            @Override
            public void mouseMoved(MouseEvent e) {
                
                Point p = e.getPoint();
                Geocache g = getMapFocusedCache(p);
               
                String tip = null;
                if( g != null )
                {
    				 tip = g.getName();
                }
                mapViewer.setToolTipText( tip );
            }
        });
	    
	    // box selection
	    MouseAdapter ma = new MouseAdapter() 
		{
			private Point start = null;
			private Point end = null;
			
			public void mouseReleased(MouseEvent e) 
			{
				if(end == null || start == null)
					return;
				
				ArrayList<Geocache> list = getMapSelectedCaches(start, e.getPoint());
				table.clearSelection();
				addToTableSelection(list);
				
				start = null;
				end = null;
				mapViewer.setPoints(null, null);
			}
			
			public void mousePressed(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1)
					start = e.getPoint();
				else
					start = null;
			}
			
			public void mouseDragged(MouseEvent e)
            {   
            	if( start == null )
            		return;
            		
                end = e.getPoint();
                mapViewer.setPoints(start, end);
            }
		};
		mapViewer.addMouseListener(ma);
		mapViewer.addMouseMotionListener(ma);
	    
	    
	    
		
		
	    
		JPanel panelBar = new JPanel();
		getContentPane().add(panelBar, BorderLayout.SOUTH);
		panelBar.setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		panelBar.add(panel, BorderLayout.EAST);
		panel.setLayout(new BorderLayout(0, 0));
		
		
		final JPanel panelCaches = new JPanel();
		panel.add(panelCaches, BorderLayout.NORTH);
		panelCaches.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		
		lblCacheCount = new JLabel("0");
		panelCaches.add(lblCacheCount);
		
		JLabel lblCaches = new JLabel("Caches");
		panelCaches.add(lblCaches);
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.SOUTH);
		
		lblLblwaypointscount = new JLabel("0 Waypoints");
		lblLblwaypointscount.setFont(new Font("Dialog", Font.BOLD, 10));
		panel_1.add(lblLblwaypointscount);
		
		JPanel panelButtons = new JPanel();
		panelBar.add(panelButtons, BorderLayout.WEST);
		
		final JToggleButton tglbtnList = new JToggleButton("List");
		tglbtnList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				scrollPane.setVisible(tglbtnList.isSelected());
				fixSplitPanes(splitPane1, splitPane2);
			}
		});
		tglbtnList.setSelected(true);
		panelButtons.add(tglbtnList);
		
		final JToggleButton tglbtnMap = new JToggleButton("Map");
		tglbtnMap.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				panelMap.setVisible(tglbtnMap.isSelected());
				fixSplitPanes(splitPane1, splitPane2);
				
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						getMapViewer().setDisplayToFitMapMarkers();
						
					}
				});
				
			}
		});
		
		final JToggleButton tglbtnCache = new JToggleButton("Cache");
		tglbtnCache.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				panelCache.setVisible(tglbtnCache.isSelected());
				fixSplitPanes(splitPane1, splitPane2);
			}
		});
		panelButtons.add(tglbtnCache);
		panelButtons.add(tglbtnMap);
	
		table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).getParent().remove(
				KeyStroke.getKeyStroke('C', Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).getParent().remove(
				KeyStroke.getKeyStroke('V', Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).getParent().remove(
				KeyStroke.getKeyStroke('X', Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).getParent().remove(
				KeyStroke.getKeyStroke('A', Toolkit.getDefaultToolkit ().getMenuShortcutKeyMask()));
		

		
//		tglbtnCache.doClick();
//		tglbtnMap.doClick();
		
	}
	
	public void updateCachePanelToSelection()
	{
		CacheListController.CLCTableModel model = (CacheListController.CLCTableModel)table.getModel();
		if( table.getSelectedRows().length == 1 )
		{
			Geocache g = model.getObject( table.getSelectedRow() );
			panelCache.setCache(g);
		}	
		if( table.getSelectedRows().length == 0 )
			panelCache.setCache(null);
	}
	
	private boolean doNotUpdateMakers = false;
	public void updateMapMarkers()
	{
		if( doNotUpdateMakers )
			return;
		
		mapViewer.removeAllMapMarkers();
		
		CacheListController.CLCTableModel tableModel = (CacheListController.CLCTableModel)table.getModel();
		if( table.getSelectedRows().length > 0 )
			for( int selection : table.getSelectedRows() )
			{
				Geocache g = tableModel.getObject( selection );
				addMapMarker(g);
			}
		else
			for( Geocache g : clc.getModel().getList() )
				addMapMarker(g);
		
		mapViewer.setDisplayToFitMapMarkers();
	}
	
	private void addMapMarker(Geocache g)
	{
		MapMarkerDot mmd = new MapMarkerCache(g);
		mapViewer.addMapMarker( mmd );
	}
	
	private class MapMarkerCache extends MapMarkerDot
	{
		private Geocache g;

		public MapMarkerCache(Geocache g) {
			super(new org.openstreetmap.gui.jmapviewer.Coordinate(
					g.getCoordinate().getLat(), 
					g.getCoordinate().getLon()) );
			this.g = g;
			
			setName("");
			
			if( g.getType() == Geocache.getTradiType() )
				setColor(new Color(0x009900));
			else if(g.getType() == Geocache.getMultiType())
				setColor(new Color(0xFFCC00));
			else if(g.getType() == Geocache.getMysteryType())
				setColor(new Color(0x0066FF));
			else
				setColor(Color.GRAY);
		}
		
		public void setColor(Color c)
		{
			super.setColor(Color.BLACK);
			super.setBackColor(c);
		}
		
		public Geocache getCache(){
			return g;
		}
	}
	
	private ArrayList<Geocache> getMapSelectedCaches(Point p1, Point p2)
	{
		ArrayList<Geocache> list = new ArrayList<>();
		if( p1 == null || p2 == null)
			return list;
		
		int x1 = p1.x < p2.x ? p1.x : p2.x;
		int x2 = p1.x >= p2.x ? p1.x : p2.x;
		int y1 = p1.y < p2.y ? p1.y : p2.y;
		int y2 = p1.y >= p2.y ? p1.y : p2.y;
		
		for(MapMarker mm : mapViewer.getMapMarkerList())
		{
			MapMarkerCache mmc = (MapMarkerCache) mm;
			Point makerPos = mapViewer.getMapPosition(mm.getLat(), mm.getLon());
			
			if( makerPos != null && 
					makerPos.x >= x1 && makerPos.x <= x2 &&  
					makerPos.y >= y1 && makerPos.y <= y2 )
			{
				list.add( mmc.getCache() );
			}
		}
		return list;
	}
	
	public void addToTableSelection(Geocache g)
	{
		ArrayList<Geocache> list = new ArrayList<>();
		list.add(g);
		addToTableSelection(list);
	}
	
	public void addToTableSelection(final ArrayList<Geocache> list_in)
	{
		doNotUpdateMakers = true;
		
//		CacheListController.CLCTableModel tableModel = (CacheListController.CLCTableModel)table.getModel();
//		for(int i=0; i < table.getRowCount(); i++)
//		{
//			Geocache gTable = tableModel.getObject( i );
//			
//			for(Geocache g : list)
//				if( gTable == g )
//				{
//					table.addRowSelectionInterval(i, i);
//					break;
//				}
//		}
		
		LinkedList<Geocache> list = new LinkedList<>();
		list.addAll(list_in);
		
		CacheListController.CLCTableModel tableModel = (CacheListController.CLCTableModel)table.getModel();
		for(int i=0; !list_in.isEmpty() && i < table.getRowCount(); i++)
		{
			Geocache gTable = tableModel.getObject( i );
			
			Iterator<Geocache> it = list.iterator();
			while(it.hasNext())
			{
				Geocache g = it.next();
				if( gTable == g )
				{
					table.addRowSelectionInterval(i, i);	// slow -> disablUpdateMakers
					it.remove();
					break;
				}
			}
		}
		
		doNotUpdateMakers = false;
		updateMapMarkers();
	}
	
	private Geocache getMapFocusedCache(Point p)
	{
        int X = p.x+3;
        int Y = p.y+3;
        java.util.List<MapMarker> ar = mapViewer.getMapMarkerList();
        Iterator<MapMarker> i = ar.iterator();
        while (i.hasNext()) {

        	MapMarkerCache mapMarker = (MapMarkerCache) i.next();

            Point MarkerPosition = mapViewer.getMapPosition(mapMarker.getLat(), mapMarker.getLon());
            if( MarkerPosition != null){

                int centerX =  MarkerPosition.x;
                int centerY = MarkerPosition.y;

                // calculate the radius from the touch to the center of the dot
                double radCircle  = Math.sqrt( (((centerX-X)*(centerX-X)) + (centerY-Y)*(centerY-Y)));

                
                if (radCircle < 10){
                	return mapMarker.getCache();
                }
            }
        }
        
        return null;
	}
	
	
	public ArrayList<Geocache> getSelectedCaches()
	{
		CacheListController.CLCTableModel model = (CacheListController.CLCTableModel)table.getModel();
		ArrayList<Geocache> selected = new ArrayList<>();
		for( int row : table.getSelectedRows() )
		{
			Geocache g = model.getObject( row );
//			panelCache.setCache(g);
			selected.add(g);
		}	
		return selected;
	}
	
	
	public void resetView()
	{
		((AbstractTableModel)table.getModel()).fireTableDataChanged();
		panelCache.setCache(null);
	}
	
	public static void fixSplitPanes(JSplitPane pane1, JSplitPane pane2)
	{
		if( fixSplitPane(pane2, 0.5) )
		{
			fixSplitPane(pane1, 0.3);
		}
		else
		{
			fixSplitPane(pane1, 0.5);
		}
	}
	
	public static boolean fixSplitPane(JSplitPane pane, double dividerLocation)
	{
		boolean retVal;
		pane.setVisible(pane.getLeftComponent().isVisible() || pane.getRightComponent().isVisible());
		if( pane.getLeftComponent().isVisible() && pane.getRightComponent().isVisible() )
		{
			pane.setDividerSize(new JSplitPane().getDividerSize());
			pane.setDividerLocation(dividerLocation);
			retVal = true;
		}
		else
		{
			pane.setDividerSize(0);
			retVal = false;
		}
		
		pane.revalidate();
		pane.repaint();
		return retVal;
	}
	
	public void setCacheCount(Integer count) {
		lblCacheCount.setText(count.toString());
	}
	
	public void setWaypointCount(Integer count, Integer orphans){
		String text = count.toString() + " Waypoints";
		if( orphans > 0 )
			text = text + " (" + orphans.toString() + " Orphans)";
		lblLblwaypointscount.setText(text);
	}
	
	public void addFilter(final CacheListFilterModel filter)
	{
		filter.addRemoveAction(new Runnable() {
			public void run() {
				clc.removeFilter(filter);
			}
		});
		filter.addFilterUpdateAction(new Runnable() {
			public void run() {
				clc.filtersUpdated();
			}
		});
		
		panelFilters.add(filter);
		panelFilters.setVisible(true);
		panelFilters.revalidate();
	}
	
	public void tableSelectAllNone()
	{
		if( table.getSelectedRowCount() == table.getRowCount() )
			table.clearSelection();
		else
			table.selectAll();
	}

	public JTable getTable() {
		return table;
	}
	public JLabel getLblCacheCount() {
		return lblCacheCount;
	}
	public JMapViewer getMapViewer() {
		return mapViewer;
	}
}
