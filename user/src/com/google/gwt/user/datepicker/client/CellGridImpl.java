/*
 * Copyright 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.gwt.user.datepicker.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.UIObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Highlighting, selectable cell grid. Used to help construct the default
 * calendar view.
 * 
 * @param <V> type of value in grid.
 */
@SuppressWarnings("unchecked")
abstract class CellGridImpl<V> extends Grid {

  /**
   * Cell type.
   */
  public abstract class Cell extends UIObject {
    private boolean enabled = true;
    private V value;
    private int index;

    /**
     * Create a cell grid.
     * 
     * @param elem the wrapped element
     * @param value the value
     */
    public Cell(Element elem, V value) {
      this.value = value;
      Cell current = this;
      index = cellList.size();
      cellList.add(current);

      setElement(elem);
      elementToCell.put(current);
    }

    public V getValue() {
      return value;
    }

    public boolean isEnabled() {
      return enabled;
    }

    public boolean isHighlighted() {
      return this == highlightedCell;
    }

    public boolean isSelected() {
      return selectedCell == this;
    }

    public final void setEnabled(boolean enabled) {
      this.enabled = enabled;
      onEnabled(enabled);
    }

    public void verticalNavigation(int keyCode) {
      switch (keyCode) {
        case KeyCodes.KEY_UP:
          setHighlighted(previousItem());
          break;
        case KeyCodes.KEY_DOWN:
          setHighlighted(nextItem());
          break;
        case KeyCodes.KEY_ESCAPE:
          // Figure out new event for this.
          break;
        case KeyCodes.KEY_ENTER:
          setSelected(this);
          break;
      }
    }

    protected Cell nextItem() {
      if (index == getLastIndex()) {
        return cellList.get(0);
      } else {
        return cellList.get(index + 1);
      }
    }

    protected void onEnabled(boolean enabled) {
      updateStyle();
    }

    protected void onHighlighted(boolean highlighted) {
      updateStyle();
    }

    protected void onSelected(boolean selected) {
      updateStyle();
    }

    protected Cell previousItem() {
      if (index != 0) {
        return cellList.get(index - 1);
      } else {
        return cellList.get(getLastIndex());
      }
    }

    protected abstract void updateStyle();

    private int getLastIndex() {
      return cellList.size() - 1;
    }
  }

  private Cell highlightedCell;

  private Cell selectedCell;
  private ElementMapper<Cell> elementToCell = new ElementMapper<Cell>();
  private ArrayList<Cell> cellList = new ArrayList<Cell>();

  protected CellGridImpl() {
    setCellPadding(0);
    setCellSpacing(0);
    setBorderWidth(0);
    sinkEvents(Event.ONCLICK | Event.ONMOUSEOVER | Event.ONMOUSEOUT);
  }

  public Cell getCell(Element element) {
    return elementToCell.get((com.google.gwt.user.client.Element) element);
  }

  public Cell getCell(Event e) {
    // Find out which cell was actually clicked.
    Element td = getEventTargetCell(e);
    return td != null
        ? elementToCell.get((com.google.gwt.user.client.Element) td) : null;
  }

  public Cell getCell(int i) {
    return cellList.get(i);
  }

  public Iterator getCells() {
    return cellList.iterator();
  }

  public Cell getHighlightedCell() {
    return highlightedCell;
  }

  public int getNumCells() {
    return cellList.size();
  }

  public Cell getSelectedCell() {
    return selectedCell;
  }

  public V getSelectedValue() {
    return getValue(selectedCell);
  }

  public V getValue(Cell cell) {
    return (cell == null ? null : cell.getValue());
  }

  @Override
  public void onBrowserEvent(Event event) {
    switch (DOM.eventGetType(event)) {
      case Event.ONCLICK: {
        Cell cell = getCell(event);
        if (isActive(cell)) {
          setSelected(cell);
        }
        break;
      }
      case Event.ONMOUSEOUT: {
        Element e = DOM.eventGetFromElement(event);
        if (e != null) {
          Cell cell = elementToCell.get((com.google.gwt.user.client.Element) e);
          if (cell == highlightedCell) {
            setHighlighted(null);
          }
        }
        break;
      }
      case Event.ONMOUSEOVER: {
        Element e = DOM.eventGetToElement(event);
        if (e != null) {
          Cell cell = elementToCell.get((com.google.gwt.user.client.Element) e);
          if (isActive(cell)) {
            setHighlighted(cell);
          }
        }
        break;
      }
    }
  }

  @Override
  public void onUnload() {
    setHighlighted(null);
  }

  public final void setHighlighted(Cell nextHighlighted) {
    if (nextHighlighted == highlightedCell) {
      return;
    }
    Cell oldHighlighted = highlightedCell;
    highlightedCell = nextHighlighted;
    if (oldHighlighted != null) {
      oldHighlighted.onHighlighted(false);
    }
    if (highlightedCell != null) {
      highlightedCell.onHighlighted(true);
    }
  }

  public final void setSelected(Cell cell) {
    Cell last = getSelectedCell();
    selectedCell = cell;

    if (last != null) {
      last.onSelected(false);
    }
    if (selectedCell != null) {
      selectedCell.onSelected(true);
    }
    onSelected(last, selectedCell);
  }

  protected void onKeyDown(Cell lastHighlighted, KeyDownEvent event) {
    if (event.isAnyModifierKeyDown()) {
      return;
    }
    int keyCode = event.getNativeKeyCode();
    if (lastHighlighted == null) {
      if (keyCode == KeyCodes.KEY_DOWN && cellList.size() > 0) {
        setHighlighted(cellList.get(0));
      }
    } else {
      lastHighlighted.verticalNavigation(keyCode);
    }
  }

  protected abstract void onSelected(Cell lastSelected, Cell cell);

  private boolean isActive(Cell cell) {
    return cell != null && cell.isEnabled();
  }
}
