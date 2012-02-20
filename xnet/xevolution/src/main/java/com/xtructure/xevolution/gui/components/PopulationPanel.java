/*
 * Copyright 2012 Michael Roberts
 * All rights reserved.
 *
 *
 * This file is part of xevolution.
 *
 * xevolution is free software: you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by the 
 * Free Software Foundation, either version 3 of the License, or (at your 
 * option) any later version.
 *
 * xevolution is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License 
 * along with xevolution.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.xtructure.xevolution.gui.components;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

import javolution.xml.stream.XMLStreamException;

import com.xtructure.xevolution.evolution.EvolutionStrategy;
import com.xtructure.xevolution.genetics.Genome;
import com.xtructure.xevolution.genetics.Population;
import com.xtructure.xevolution.gui.XEvolutionGui;
import com.xtructure.xutil.id.XValId;
import com.xtructure.xutil.xml.XmlReader;

/**
 * The {@link PopulationPanel} holds the populations list and population data in
 * the "Generations" tab in {@link XEvolutionGui}. The list represents all the
 * populations that have been generated by a run of an {@link EvolutionStrategy}
 * , and the data of the selected population is displayed here.
 * <p>
 * Only the population xml {@link File}s are kept in memory. When one is
 * selected, the file is read and a new population is instantiated.
 * 
 * @author Luis Guimbarda
 * 
 */
public class PopulationPanel extends JPanel {
	private static final long			serialVersionUID	= 1L;
	/** the base title string for this {@link PopulationPanel} */
	private final String				titleBase			= "Populations";
	/** the border for this {@link PopulationPanel} */
	private final TitledBorder			border				= BorderFactory.createTitledBorder(titleBase);
	/** the population read from the selected population file */
	private Population<Genome<?>>		population			= null;
	/** the component to display the data for the selected {@link Population} */
	private final PopulationDataPanel	dataPanel;
	/** the component to display the list of population xml files */
	private final JComboBox				comboBox;
	/** lock for setting population data */
	private final ReentrantLock			popLock;

	/**
	 * Creates a new {@link PopulationPanel}
	 * 
	 * @param popLock
	 *            lock used by parent for reading population xml files
	 */
	public PopulationPanel(ReentrantLock popLock) {
		this.popLock = popLock;
		setBorder(border);
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.insets = new Insets(0, 2, 2, 2);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		comboBox = new JComboBox();
		add(comboBox, c);
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.5;
		c.weighty = 0.5;
		c.gridx = 0;
		c.gridy = 1;
		dataPanel = new PopulationDataPanel();
		add(dataPanel, c);
	}

	/**
	 * Adds all of the given population xml {@link File}s to this
	 * {@link PopulationPanel}'s list of navigable populations.
	 * 
	 * @param populationFiles
	 *            the {@link File}s to add
	 */
	public void addAllItems(List<File> populationFiles) {
		for (File file : populationFiles) {
			comboBox.addItem(new AliasedPopulationFile(file));
		}
		border.setTitle(String.format("%s (%d)", titleBase, comboBox.getItemCount()));
		repaint();
	}

	/**
	 * Clears this {@link PopulationPanel} of all navigable {@link Population}s
	 */
	public void removeAllItems() {
		comboBox.removeAllItems();
		border.setTitle(String.format("%s (%d)", titleBase, comboBox.getItemCount()));
		repaint();
	}

	/**
	 * Updates this {@link PopulationPanel} data panel with the data from the
	 * selected {@link Population}
	 */
	public void setLabels() {
		popLock.lock();
		try {
			File populationFile = ((AliasedPopulationFile) comboBox.getSelectedItem()).getPopulationFile();
			population = XmlReader.read(populationFile);
			if (population == null) {
				return;
			}
			population.refreshStats();
			dataPanel.setLabels(population);
		} catch (XMLStreamException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			popLock.unlock();
		}
	}

	/**
	 * Returns the comboBox component of this {@link PopulationPanel}
	 * 
	 * @return the comboBox component of this {@link PopulationPanel}
	 */
	public JComboBox getComboBox() {
		return comboBox;
	}

	/**
	 * Returns the selected {@link Population} of this {@link PopulationPanel}
	 * 
	 * @return the selected {@link Population} of this {@link PopulationPanel}
	 */
	public Population<?> getPopulation() {
		return population;
	}

	/** the data panel class for {@link PopulationPanel} */
	private static final class PopulationDataPanel extends JPanel {
		private static final long		serialVersionUID	= 1L;
		/** the {@link JLabel} for the {@link Population}'s id */
		private final JLabel			idLabel;
		/** the {@link JLabel} for the {@link Population}'s size */
		private final JLabel			sizeLabel;
		/** the {@link JLabel} for the {@link Population}'s age */
		private final JLabel			ageLabel;
		/** the {@link JLabel} fothe the {@link Population}'s ageLastImproved */
		private final JLabel			ageLastImprovedLabel;
		/** a text panel for other information about the {@link Population} */
		private final NumberedTextPanel	graphTextPanel;

		/**
		 * Creates a new {@link PopulationDataPanel}
		 */
		public PopulationDataPanel() {
			setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.insets = new Insets(0, 2, 2, 2);
			c.anchor = GridBagConstraints.FIRST_LINE_START;
			int row = 0;
			c.fill = GridBagConstraints.NONE;
			c.weighty = 0.0;
			c.weightx = 0.0;
			c.gridx = 0;
			c.gridy = row;
			add(new JLabel("Id: "), c);
			idLabel = new JLabel("-");
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = row;
			add(idLabel, c);
			row++;
			c.weightx = 0.0;
			c.gridx = 0;
			c.gridy = row;
			add(new JLabel("Size: "), c);
			sizeLabel = new JLabel("-");
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = row;
			add(sizeLabel, c);
			row++;
			c.weightx = 0.0;
			c.gridx = 0;
			c.gridy = row;
			add(new JLabel("Age: "), c);
			ageLabel = new JLabel("-");
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = row;
			add(ageLabel, c);
			row++;
			c.weightx = 0.0;
			c.gridx = 0;
			c.gridy = row;
			add(new JLabel("Age last improved: "), c);
			ageLastImprovedLabel = new JLabel("-");
			c.weightx = 0.5;
			c.gridx = 1;
			c.gridy = row;
			add(ageLastImprovedLabel, c);
			row++;
			graphTextPanel = new NumberedTextPanel();
			c.fill = GridBagConstraints.BOTH;
			c.weighty = 0.5;
			c.weightx = 0.0;
			c.gridx = 0;
			c.gridy = row;
			c.gridwidth = 2;
			add(new JScrollPane(graphTextPanel), c);
			row++;
		}

		/**
		 * Sets this {@link PopulationDataPanel}'s data according to the given
		 * population
		 * 
		 * @param population
		 *            the population whose data to set
		 */
		public void setLabels(Population<?> population) {
			idLabel.setText(population.getId().toString());
			sizeLabel.setText(Integer.toString(population.size()));
			ageLabel.setText(Long.toString(population.getAge()));
			ageLastImprovedLabel.setText(Long.toString(population.getAgeLastImproved()));
			idLabel.setToolTipText(population.getId().toString());
			sizeLabel.setToolTipText(Integer.toString(population.size()));
			ageLabel.setToolTipText(Long.toString(population.getAge()));
			ageLastImprovedLabel.setToolTipText(Long.toString(population.getAgeLastImproved()));
			StringBuilder sb = new StringBuilder();
			for (@SuppressWarnings("rawtypes")
			XValId valueId : population.getGenomeAttributeIds()) {
				Genome<?> highGenome = population.getHighestGenomeByAttribute(valueId);
				Genome<?> lowGenome = population.getLowestGenomeByAttribute(valueId);
				@SuppressWarnings("unchecked")
				Object high = highGenome.getAttribute(valueId);
				@SuppressWarnings("unchecked")
				Object low = lowGenome.getAttribute(valueId);
				Double avg = population.getAverageGenomeAttribute(valueId);
				sb.append(valueId.getBase() + "\n");
				sb.append(String.format("\thigh    (%s) : %s\n", highGenome.getId(), high));
				sb.append(String.format("\tlow     (%s) : %s\n", lowGenome.getId(), low));
				sb.append(String.format("\taverage : %s\n", avg));
			}
			graphTextPanel.setText(sb.toString());
		}
	}
}