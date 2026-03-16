package cn.com.toolkit.tools.cron.control;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class SelectGrid extends FlowPane {
    private final AtomicInteger countAtomic = new AtomicInteger(0);
    private final String[] toggleBackgroundColor;
    private final String[] enteredBackgroundColor;
    private final List<GridCell> gridCellList = new ArrayList<>();
    private Consumer<List<String>> selectedConsumer;
    private final int cellWidth;
    private final int cellHeight;
    private int dragBeginIndex = -1;
    private int dragBeginSelected = -1;
    private int dragCurrentMinIndex = -1;
    private int dragCurrentMaxIndex = -1;

    public SelectGrid() {
        this(35,35,1,1
                ,"-color-accent-subtle","-color-accent-muted"
        ,"-color-success-subtle","-color-success-muted");
    }
    public SelectGrid(int cellWidth, int cellHeight
            ,int vgap,int hgap
            ,String unselectBgc,String selectBgc
            ,String enteredUnselectBgc,String enteredSelectBgc) {
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.setVgap(vgap);
        this.setHgap(hgap);
        this.toggleBackgroundColor = new String[]{unselectBgc,selectBgc};
        this.enteredBackgroundColor = new String[]{enteredUnselectBgc,enteredSelectBgc};
    }

    public void addCell(String text){
        GridCell gridCell = new GridCell(text);
        this.getChildren().add(gridCell);
        gridCellList.add(gridCell);
    }
    public List<String> getSelectedIndex(){
        return gridCellList.stream()
                .filter(e -> e.getSelected() == 1)
                .map(GridCell::getIndex)
                .map(String::valueOf)
                .toList();
    }
    public void onSelectedChanged(Consumer<List<String>> selectedConsumer){
        this.selectedConsumer = selectedConsumer;
    }

    class GridCell extends Label{
        private int selected;
        private int index;

        public GridCell(String s) {
            super(s);
            this.index = countAtomic.getAndIncrement();
            this.setAlignment(Pos.BASELINE_CENTER);
            this.setPrefWidth(cellWidth);
            this.setPrefHeight(cellHeight);
            setSelected(0);

            this.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> toggleSelect());
            this.addEventHandler(MouseEvent.MOUSE_ENTERED, event -> {
                this.setStyle("-fx-background-color: " + enteredBackgroundColor[getSelected()] + ";");
            });
            this.addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
                this.setSelected(getSelected());
            });
            this.addEventHandler(MouseDragEvent.DRAG_DETECTED, event -> startFullDrag());
            this.addEventHandler(MouseEvent.MOUSE_PRESSED,event -> {
                dragBeginIndex = this.getIndex();
                dragCurrentMinIndex = this.getIndex();
                dragCurrentMaxIndex = this.getIndex();
                dragBeginSelected = this.getSelected();
                event.consume();
            });
            this.addEventHandler(MouseDragEvent.MOUSE_DRAG_OVER, event -> {
                int min = Math.min(dragBeginIndex,this.getIndex());
                int max = Math.max(dragBeginIndex,this.getIndex());
                dragCurrentMinIndex = Math.min(dragCurrentMinIndex,min);
                dragCurrentMaxIndex = Math.max(dragCurrentMaxIndex,max);
                int selected = dragBeginSelected ^ 1;
                for(int i = 0; i < gridCellList.size(); i++){
                    GridCell gridCell = gridCellList.get(i);
                    if(dragCurrentMinIndex < min && i < min && i >= dragCurrentMinIndex && gridCell.getSelected() != dragBeginSelected)
                        gridCell.setSelected(dragBeginSelected);
                    else if(dragCurrentMaxIndex > max && i > max && i <= dragCurrentMaxIndex && gridCell.getSelected() != dragBeginSelected)
                        gridCell.setSelected(dragBeginSelected);
                    else if(i >= min && i <= max && gridCell.getSelected() != selected)
                            gridCell.setSelected(selected);
                }
                if(selectedConsumer != null) selectedConsumer.accept(getSelectedIndex());
                event.consume();
            });
        }

        public void toggleSelect(){
            this.setSelected(selected ^ 1);
            if(selectedConsumer != null) selectedConsumer.accept(getSelectedIndex());
        }
        public void setSelected(int selected) {
            this.selected = selected;
            this.setStyle("-fx-background-color: " + toggleBackgroundColor[selected] + ";");
        }
        public int getIndex() {
            return index;
        }
        public void setIndex(int index) {
            this.index = index;
        }
        public int getSelected() {
            return selected;
        }
    }
}
