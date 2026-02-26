package cn.com.toolkit.tools.cron.controller;


import cn.com.toolkit.framework.core.util.Notifications;
import cn.com.toolkit.tools.cron.controls.SelectGrid;
import cn.com.toolkit.tools.cron.enums.WeekDay;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import javafx.beans.InvalidationListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.reactfx.util.TriFunction;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

public class CronToolController {
    @FXML private RadioButton selectSecondRadioButton;
    @FXML private SelectGrid selectSecondSelectGrid;
    @FXML private RadioButton selectMinuteRadioButton;
    @FXML private SelectGrid selectMinuteSelectGrid;
    @FXML private RadioButton selectHourRadioButton;
    @FXML private SelectGrid selectHourSelectGrid;
    @FXML private RadioButton selectDayRadioButton;
    @FXML private SelectGrid selectDaySelectGrid;
    @FXML private RadioButton selectMonthRadioButton;
    @FXML private SelectGrid selectMonthSelectGrid;
    @FXML private RadioButton selectWeekRadioButton;
    @FXML private SelectGrid selectWeekSelectGrid;
    @FXML private RadioButton eachSecondRadioButton;
    @FXML private RadioButton periodEachSecondRadioButton;
    @FXML private Spinner<Integer> periodEachSecondBeginSpinner;
    @FXML private Spinner<Integer> periodEachSecondEndSpinner;
    @FXML private RadioButton periodSecondRadioButton;
    @FXML private Spinner<Integer> periodSecondBeginSpinner;
    @FXML private Spinner<Integer> periodSecondEndSpinner;
    @FXML private RadioButton eachMinuteRadioButton;
    @FXML private RadioButton periodEachMinuteRadioButton;
    @FXML private RadioButton periodMinuteRadioButton;
    @FXML private Spinner<Integer> periodEachMinuteBeginSpinner;
    @FXML private Spinner<Integer> periodEachMinuteEndSpinner;
    @FXML private Spinner<Integer> periodMinuteBeginSpinner;
    @FXML private Spinner<Integer> periodMinuteEndSpinner;
    @FXML private RadioButton eachHourRadioButton;
    @FXML private RadioButton periodEachHourRadioButton;
    @FXML private Spinner<Integer> periodEachHourBeginSpinner;
    @FXML private Spinner<Integer> periodEachHourEndSpinner;
    @FXML private RadioButton periodHourRadioButton;
    @FXML private Spinner<Integer> periodHourBeginSpinner;
    @FXML private Spinner<Integer> periodHourEndSpinner;
    @FXML private RadioButton eachDayRadioButton;
    @FXML private RadioButton unselectDayRadioButton;
    @FXML private RadioButton periodEachDayRadioButton;
    @FXML private Spinner<Integer> periodEachDayBeginSpinner;
    @FXML private Spinner<Integer> periodEachDayEndSpinner;
    @FXML private RadioButton periodDayRadioButton;
    @FXML private Spinner<Integer> periodDayBeginSpinner;
    @FXML private Spinner<Integer> periodDayEndSpinner;
    @FXML private RadioButton dayOfMonthRadioButton;
    @FXML private Spinner<Integer> dayOfMonthSpinner;
    @FXML private RadioButton endOfMonthRadioButton;
    @FXML private RadioButton eachMonthRadioButton;
    @FXML private RadioButton unselectMonthRadioButton;
    @FXML private RadioButton periodEachMonthRadioButton;
    @FXML private Spinner<Integer> periodEachMonthBeginSpinner;
    @FXML private Spinner<Integer> periodEachMonthEndSpinner;
    @FXML private RadioButton periodMonthRadioButton;
    @FXML private Spinner<Integer> periodMonthBeginSpinner;
    @FXML private Spinner<Integer> periodMonthEndSpinner;
    @FXML private RadioButton eachWeekRadioButton;
    @FXML private RadioButton unselectWeekRadioButton;
    @FXML private RadioButton periodEachWeekRadioButton;
    @FXML private RadioButton weekBeginRadioButton;
    @FXML private Spinner<Integer> weekEndSpinner;
    @FXML private ChoiceBox<WeekDay> periodEachWeekBeginChoiceBox;
    @FXML private ChoiceBox<WeekDay> periodEachWeekEndChoiceBox;
    @FXML private ChoiceBox<WeekDay> weekBeginChoiceBox;
    @FXML private ChoiceBox<WeekDay> endWeekOfMonthChoiceBox;
    @FXML private RadioButton endWeekOfMonthRadioButton;
    @FXML private RadioButton eachYearRadioButton;
    @FXML private RadioButton unselectYearRadioButton;
    @FXML private RadioButton periodEachYearRadioButton;
    @FXML private Spinner<Integer> periodEachYearBeginSpinner;
    @FXML private Spinner<Integer> periodEachYearEndSpinner;
    @FXML private TextField cronSecondTextField;
    @FXML private TextField cronMinuteTextField;
    @FXML private TextField cronHourTextField;
    @FXML private TextField cronDayTextField;
    @FXML private TextField cronMonthTextField;
    @FXML private TextField cronWeekTextField;
    @FXML private TextField cronYearTextField;
    @FXML private TextArea cornExpResultTextArea;
    @FXML private Button copyCornExpButton;
    @FXML private Button executeCornExpButton;
    @FXML private ListView<String> scheduleListView;


    @FXML
    public void initialize(){
        validSpinnerRange(periodEachSecondBeginSpinner,periodEachSecondEndSpinner);
        validSpinnerRange(periodEachMinuteBeginSpinner,periodEachMinuteEndSpinner);
        validSpinnerRange(periodEachHourBeginSpinner,periodEachHourEndSpinner);
        validSpinnerRange(periodEachDayBeginSpinner,periodEachDayEndSpinner);
        validSpinnerRange(periodEachMonthBeginSpinner,periodEachMonthEndSpinner);
        validSpinnerRange(periodEachYearBeginSpinner,periodEachYearEndSpinner);
        validChoiceBoxRange(periodEachWeekBeginChoiceBox,periodEachWeekEndChoiceBox);
        periodEachYearBeginSpinner.getValueFactory().setValue(LocalDate.now().getYear());
        periodEachYearEndSpinner.getValueFactory().setValue(LocalDate.now().getYear() + 1);
        initCornExpResultTextArea();
        initButtonAction();
        initExpChange();
        initSelectGrid();
    }
    private void initExpChange(){
        Stream.of(eachSecondRadioButton, periodEachSecondRadioButton, periodSecondRadioButton,selectSecondRadioButton)
                .forEach(button -> button.addEventHandler(ActionEvent.ACTION, event -> computeSecondExp()));
        Stream.of(periodEachSecondBeginSpinner,periodEachSecondEndSpinner,periodSecondBeginSpinner,periodSecondEndSpinner)
                .forEach(spinner -> spinner.valueProperty().addListener(observable -> computeSecondExp()));

        Stream.of(eachMinuteRadioButton, periodEachMinuteRadioButton, periodMinuteRadioButton,selectMinuteRadioButton)
                .forEach(button -> button.addEventHandler(ActionEvent.ACTION, event -> computeMinuteExp()));
        Stream.of(periodEachMinuteBeginSpinner,periodEachMinuteEndSpinner,periodMinuteBeginSpinner,periodMinuteEndSpinner)
                .forEach(spinner -> spinner.valueProperty().addListener(observable -> computeMinuteExp()));

        Stream.of(eachHourRadioButton, periodEachHourRadioButton, periodHourRadioButton,selectHourRadioButton)
                .forEach(button -> button.addEventHandler(ActionEvent.ACTION, event -> computeHourExp()));
        Stream.of(periodEachHourBeginSpinner,periodEachHourEndSpinner,periodHourBeginSpinner,periodHourEndSpinner)
                .forEach(spinner -> spinner.valueProperty().addListener(observable -> computeHourExp()));

        Stream.of(eachDayRadioButton, unselectDayRadioButton, periodEachDayRadioButton,periodDayRadioButton,dayOfMonthRadioButton,endOfMonthRadioButton,selectDayRadioButton)
                .forEach(button -> button.addEventHandler(ActionEvent.ACTION, event -> computeDayExp()));
        Stream.of(periodEachDayBeginSpinner,periodEachDayEndSpinner,periodDayBeginSpinner,periodDayEndSpinner,dayOfMonthSpinner)
                .forEach(spinner -> spinner.valueProperty().addListener(observable -> computeDayExp()));

        Stream.of(eachMonthRadioButton, unselectMonthRadioButton, periodEachMonthRadioButton,periodMonthRadioButton,selectMonthRadioButton)
                .forEach(button -> button.addEventHandler(ActionEvent.ACTION, event -> computeMonthExp()));
        Stream.of(periodEachMonthBeginSpinner,periodEachMonthEndSpinner,periodMonthBeginSpinner,periodMonthEndSpinner)
                .forEach(spinner -> spinner.valueProperty().addListener(observable -> computeMonthExp()));

        Stream.of(eachWeekRadioButton, unselectWeekRadioButton, periodEachWeekRadioButton,weekBeginRadioButton,endWeekOfMonthRadioButton
                ,periodEachWeekBeginChoiceBox,periodEachWeekEndChoiceBox,weekBeginChoiceBox,endWeekOfMonthChoiceBox,selectWeekRadioButton)
                .forEach(button -> button.addEventHandler(ActionEvent.ACTION, event -> computeWeekExp()));
        Stream.of(weekEndSpinner)
                .forEach(spinner -> spinner.valueProperty().addListener(observable -> computeWeekExp()));

        Stream.of(eachYearRadioButton, unselectYearRadioButton, periodEachYearRadioButton)
                .forEach(button -> button.addEventHandler(ActionEvent.ACTION, event -> computeYearExp()));
        Stream.of(periodEachYearBeginSpinner,periodEachYearEndSpinner)
                .forEach(spinner -> spinner.valueProperty().addListener(observable -> computeYearExp()));
    }
    private void computeSecondExp(){
        String exp = cronSecondTextField.getText();
        if(eachSecondRadioButton.isSelected()) exp = "*";
        else if(periodEachSecondRadioButton.isSelected()){
            int begin = periodEachSecondBeginSpinner.getValue();
            int end = periodEachSecondEndSpinner.getValue();
            if(begin == end) exp = begin + "";
            else exp = begin + "-" + end;
        }
        else if(periodSecondRadioButton.isSelected())
            exp = periodSecondBeginSpinner.getValue() + "/" + periodSecondEndSpinner.getValue();
        else if(selectSecondRadioButton.isSelected())
            exp = String.join(",",selectSecondSelectGrid.getSelectedIndex());
        cronSecondTextField.setText(exp);
    }
    private void computeMinuteExp(){
        String exp = cronMinuteTextField.getText();
        if(eachMinuteRadioButton.isSelected()) exp = "*";
        else if(periodEachMinuteRadioButton.isSelected()){
            int begin = periodEachMinuteBeginSpinner.getValue();
            int end = periodEachMinuteEndSpinner.getValue();
            if(begin == end) exp = begin + "";
            else exp = begin + "-" + end;
        }
        else if(periodMinuteRadioButton.isSelected())
            exp = periodMinuteBeginSpinner.getValue() + "/" + periodMinuteEndSpinner.getValue();
        else if(selectMinuteRadioButton.isSelected())
            exp = String.join(",",selectMinuteSelectGrid.getSelectedIndex());
        cronMinuteTextField.setText(exp);
    }
    private void computeHourExp(){
        String exp = cronHourTextField.getText();
        if(eachHourRadioButton.isSelected()) exp = "*";
        else if(periodEachHourRadioButton.isSelected()){
            int begin = periodEachHourBeginSpinner.getValue();
            int end = periodEachHourEndSpinner.getValue();
            if(begin == end) exp = begin + "";
            else exp = begin + "-" + end;
        }
        else if(periodHourRadioButton.isSelected())
            exp = periodHourBeginSpinner.getValue() + "/" + periodHourEndSpinner.getValue();
        else if(selectHourRadioButton.isSelected())
            exp = String.join(",",selectHourSelectGrid.getSelectedIndex());
        cronHourTextField.setText(exp);
    }
    private void computeDayExp(){
        String exp = cronDayTextField.getText();
        if(eachDayRadioButton.isSelected()) exp = "*";
        else if(unselectDayRadioButton.isSelected()) exp = "?";
        else if(periodEachDayRadioButton.isSelected()){
            int begin = periodEachDayBeginSpinner.getValue();
            int end = periodEachDayEndSpinner.getValue();
            if(begin == end) exp = begin + "";
            else exp = begin + "-" + end;
        }
        else if(periodDayRadioButton.isSelected())
            exp = periodDayBeginSpinner.getValue() + "/" + periodDayEndSpinner.getValue();
        else if(dayOfMonthRadioButton.isSelected())
            exp = dayOfMonthSpinner.getValue() + "W";
        else if(endOfMonthRadioButton.isSelected()) exp = "L";
        else if(selectDayRadioButton.isSelected())
            exp = String.join(",",selectDaySelectGrid.getSelectedIndex());
        cronDayTextField.setText(exp);
    }
    private void computeMonthExp(){
        String exp = cronMonthTextField.getText();
        if(eachMonthRadioButton.isSelected()) exp = "*";
        else if(unselectMonthRadioButton.isSelected()) exp = "?";
        else if(periodEachMonthRadioButton.isSelected()){
            int begin = periodEachMonthBeginSpinner.getValue();
            int end = periodEachMonthEndSpinner.getValue();
            if(begin == end) exp = begin + "";
            else exp = begin + "-" + end;
        }
        else if(periodMonthRadioButton.isSelected())
            exp = periodMonthBeginSpinner.getValue() + "/" + periodMonthEndSpinner.getValue();
        else if(selectMonthRadioButton.isSelected())
            exp = String.join(",",selectMonthSelectGrid.getSelectedIndex());
        cronMonthTextField.setText(exp);
    }
    private void computeWeekExp(){
        String exp = cronWeekTextField.getText();
        if(eachWeekRadioButton.isSelected()) exp = "*";
        else if(unselectWeekRadioButton.isSelected()) exp = "?";
        else if(periodEachWeekRadioButton.isSelected()
                && periodEachWeekBeginChoiceBox.getValue() != null
                && periodEachWeekEndChoiceBox.getValue() != null){

            int begin = periodEachWeekBeginChoiceBox.getValue().getValue();
            int end = periodEachWeekEndChoiceBox.getValue().getValue();
            if(begin == end) exp = begin + "";
            else exp = begin + "-" + end;
        }
        else if(weekBeginRadioButton.isSelected() && weekBeginChoiceBox.getValue() != null)
            exp = weekEndSpinner.getValue() + "#" + weekBeginChoiceBox.getValue().getValue();
        else if(endWeekOfMonthRadioButton.isSelected() && endWeekOfMonthChoiceBox.getValue() != null)
            exp = endWeekOfMonthChoiceBox.getValue().getValue() + "L";
        else if(selectWeekRadioButton.isSelected())
            exp = String.join(",",selectWeekSelectGrid.getSelectedIndex());
        cronWeekTextField.setText(exp);
    }
    private void computeYearExp(){
        String exp = cronYearTextField.getText();
        if(eachYearRadioButton.isSelected()) exp = "*";
        else if(unselectYearRadioButton.isSelected()) exp = "";
        else if(periodEachYearRadioButton.isSelected()){
            int begin = periodEachYearBeginSpinner.getValue();
            int end = periodEachYearEndSpinner.getValue();
            if(begin == end) exp = begin + "";
            else exp = begin + "-" + end;
        }
        cronYearTextField.setText(exp);
    }
    private void initSelectGrid(){
        for(int i = 0; i < 60; ++i)
            selectSecondSelectGrid.addCell(i + "");
        selectSecondSelectGrid.onSelectedChanged(list -> computeSecondExp());

        for(int i = 0; i < 60; ++i)
            selectMinuteSelectGrid.addCell(i + "");
        selectMinuteSelectGrid.onSelectedChanged(list -> computeMinuteExp());

        for(int i = 0; i < 24; ++i)
            selectHourSelectGrid.addCell(i + "");
        selectHourSelectGrid.onSelectedChanged(list -> computeHourExp());

        for(int i = 1; i < 32; ++i)
            selectDaySelectGrid.addCell(i + "");
        selectDaySelectGrid.onSelectedChanged(list -> computeDayExp());

        for(int i = 1; i < 13; ++i)
            selectMonthSelectGrid.addCell(i + "");
        selectMonthSelectGrid.onSelectedChanged(list -> computeMonthExp());

        for(int i = 1; i < 8; ++i)
            selectWeekSelectGrid.addCell(WeekDay.valueOf(i).getDisplay());
        selectWeekSelectGrid.onSelectedChanged(list -> computeWeekExp());
    }
    private void initButtonAction(){
        //复制按钮
        copyCornExpButton.setOnAction(event -> {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            content.putString(cornExpResultTextArea.getText());
            clipboard.setContent(content);
            Notifications.success("已复制到剪切板!", Pos.TOP_RIGHT);
        });
        //运行按钮
        executeCornExpButton.setOnAction(event -> {
            CronDefinition definition = CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ);
            CronParser parser = new CronParser(definition);
            Cron cron = parser.parse(cornExpResultTextArea.getText());
            if(cron == null) throw new RuntimeException("表达式无效!");
            scheduleListView.getItems().clear();
            ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
            ExecutionTime executionTime = ExecutionTime.forCron(cron);
            for (int i = 0; i < 20; i++) {
                now = executionTime.nextExecution(now).orElse(now);
                scheduleListView.getItems().add(DateTimeFormatter
                        .ofPattern("yyyy-MM-dd HH:mm:ss")
                        .format(now));
            }
        });
    }
    private void initCornExpResultTextArea(){
        InvalidationListener invalidationListener = observable -> {
            cornExpResultTextArea.setText(String.join(" ",
                    cronSecondTextField.textProperty().getValue(),
                    cronMinuteTextField.textProperty().getValue(),
                    cronHourTextField.textProperty().getValue(),
                    cronDayTextField.textProperty().getValue(),
                    cronMonthTextField.textProperty().getValue(),
                    cronWeekTextField.textProperty().getValue(),
                    cronYearTextField.textProperty().getValue()));
        };
        cronSecondTextField.textProperty().addListener(invalidationListener);
        cronMinuteTextField.textProperty().addListener(invalidationListener);
        cronHourTextField.textProperty().addListener(invalidationListener);
        cronDayTextField.textProperty().addListener(invalidationListener);
        cronMonthTextField.textProperty().addListener(invalidationListener);
        cronWeekTextField.textProperty().addListener(invalidationListener);
        cronYearTextField.textProperty().addListener(invalidationListener);
    }
    private void validSpinnerRange(Spinner<Integer> beginSpinner,Spinner<Integer> endSpinner){
        final TriFunction<Spinner<Integer>,Spinner<Integer>,Spinner<Integer>,InvalidationListener>
                triFunction = (Spinner<Integer> begin,Spinner<Integer> end,Spinner<Integer> current) -> {
                    SpinnerValueFactory.IntegerSpinnerValueFactory beginFactory =
                            (SpinnerValueFactory.IntegerSpinnerValueFactory) begin.getValueFactory();
                    SpinnerValueFactory.IntegerSpinnerValueFactory endFactory =
                            (SpinnerValueFactory.IntegerSpinnerValueFactory) end.getValueFactory();
                    InvalidationListener invalidationListener = observable -> {};
                    if(begin == current){
                        invalidationListener = observable -> {
                            if(beginFactory.getValue() > endFactory.getValue())
                                endFactory.setValue(beginFactory.getValue());
                        };
                    }
                    else if(end == current){
                        invalidationListener = observable -> {
                            if(endFactory.getValue() < beginFactory.getValue())
                                beginFactory.setValue(endFactory.getValue());
                        };
                    }
                    return invalidationListener;
                };
        beginSpinner.valueProperty().addListener(triFunction.apply(beginSpinner,endSpinner,beginSpinner));
        endSpinner.valueProperty().addListener(triFunction.apply(beginSpinner,endSpinner,endSpinner));
    }
    private void validChoiceBoxRange(ChoiceBox<WeekDay> beginChoiceBox, ChoiceBox<WeekDay> endChoiceBox){
        EventHandler<ActionEvent> eventHandler = event -> {
            if(event.getSource() == beginChoiceBox){
                if(endChoiceBox.getValue() != null && beginChoiceBox.getValue().getValue() > endChoiceBox.getValue().getValue())
                    endChoiceBox.setValue(beginChoiceBox.getValue());
            }
            else if(event.getSource() == endChoiceBox){
                if(beginChoiceBox.getValue() != null && endChoiceBox.getValue().getValue() < beginChoiceBox.getValue().getValue())
                    beginChoiceBox.setValue(endChoiceBox.getValue());
            }
        };
        beginChoiceBox.addEventHandler(ActionEvent.ACTION, eventHandler);
        endChoiceBox.addEventHandler(ActionEvent.ACTION, eventHandler);
    }
}
