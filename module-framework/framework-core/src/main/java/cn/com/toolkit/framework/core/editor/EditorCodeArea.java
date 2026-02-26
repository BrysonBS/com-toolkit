package cn.com.toolkit.framework.core.editor;

import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.IndexRange;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.PlainTextChange;
import org.fxmisc.richtext.model.StyleSpans;

import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;
import static org.fxmisc.richtext.model.TwoDimensional.Bias.Backward;

public class EditorCodeArea extends CodeArea {
    private final SimpleBooleanProperty showLineNumberProperty = new SimpleBooleanProperty();
    private final SimpleStringProperty textProperty = new SimpleStringProperty();
    private final SimpleObjectProperty<LanguageType> languageTypeProperty = new SimpleObjectProperty<>();
    private static final Pattern whiteSpace = Pattern.compile( "^\\s+" );
    public EditorCodeArea(boolean showLineNumber,LanguageType languageType) {
        //添加css适应主题
        addOrReplaceStylesheets(this.getClass().getResource("/assets/styles/editor/code-area.css"));

        //行号
        this.showLineNumberProperty.addListener(observable -> {
            if(this.showLineNumberProperty.get())
                this.setParagraphGraphicFactory(LineNumberFactory.get(this));
        });

        //文本
        this.textProperty.addListener(observable -> this.replaceText(0,this.getText().length(),this.textProperty.get()));
        //样式
        this.languageTypeProperty.addListener((observable, oldValue, newValue) -> {
            ObservableList<String> stylesheetObservableList = getStylesheets();
            if(oldValue != null) stylesheetObservableList.removeIf(s -> Objects.equals(s,Objects.requireNonNull(this.getClass().getResource(oldValue.getKeywordsFile())).toExternalForm()));
            if(newValue == LanguageType.PLAIN_TEXT || newValue.getKeywordsFile() == null) return;
            stylesheetObservableList.add(Objects.requireNonNull(this.getClass().getResource(newValue.getKeywordsFile())).toExternalForm());
        });

        this.showLineNumberProperty.set(showLineNumber);
        this.languageTypeProperty.set(languageType);
        // 设置语法高亮
        this.multiPlainChanges()
                .reduceSuccessions((firstChanges, secondChanges) -> {
                    List<PlainTextChange> combinedChangeList = new ArrayList<>(firstChanges);
                    combinedChangeList.addAll(secondChanges);
                    return combinedChangeList;
                }, Duration.ofMillis(500))
                .subscribe(textChanges -> restyleChangedLines(this, textChanges));
        this.addEventHandler( KeyEvent.KEY_PRESSED, KE -> onKeyPressed(this, KE));
    }

    private void restyleChangedLines(CodeArea codeArea, List<PlainTextChange> textChanges) {
        Set<IndexRange> positionsChanged = getChangedRangeSet(textChanges);
        Set<Integer> lineNumbersChanged = getLineNumberChangeSet(codeArea, positionsChanged);
        restyleLines(codeArea, lineNumbersChanged);
    }
    private Set<IndexRange> getChangedRangeSet(List<PlainTextChange> textChanges){
        Set<IndexRange> changedRangeSet = new HashSet<>();
        for (PlainTextChange textChange : textChanges) {
            int changePosition = textChange.getPosition();
            int netLength = textChange.getNetLength();
            //字符长度发生变化,已创建的变化列表的索引可能会受到影响跟着变化,需动态调整
            if(netLength != 0){
                changedRangeSet = changedRangeSet.stream().map(range -> {
                    int newStart = shiftIndexAtOrAbovePosition(range.getStart(), changePosition, netLength);
                    int newEnd = shiftIndexAtOrAbovePosition(range.getEnd(), changePosition, netLength);
                    return new IndexRange(newStart, newEnd);
                }).collect(toSet());
            }
            changedRangeSet.add(new IndexRange(textChange.getPosition(), textChange.getInsertionEnd()));
        }
        return changedRangeSet;
    }
    private int shiftIndexAtOrAbovePosition(int index, int changePosition, int shift) {
        if (index < changePosition) return index;
        return Math.max(index + shift, changePosition);
    }
    private Set<Integer> getLineNumberChangeSet(CodeArea codeArea,Set<IndexRange> changeRangeSet){
        return changeRangeSet.stream()
                .flatMap(indexRange -> {
                    int startLineNumber = codeArea.offsetToPosition(indexRange.getStart(), Backward).getMajor();
                    int endPosition = indexRange.getEnd();
                    int endLineNumber = endPosition == indexRange.getStart() ? startLineNumber : codeArea.offsetToPosition(endPosition, Backward).getMajor();
                    return IntStream.rangeClosed(startLineNumber, endLineNumber).boxed();
                })
                .collect(toSet());
    }
    //换行添加自动缩进
    private void onKeyPressed(CodeArea codeArea, KeyEvent KE) {
        if ( KE.getCode() == KeyCode.ENTER ) {
            int caretPosition = codeArea.getCaretPosition();
            int currentParagraph = codeArea.getCurrentParagraph();
            Matcher m0 = whiteSpace.matcher( codeArea.getParagraph( currentParagraph-1 ).getSegments().getFirst() );
            if (m0.find()) Platform.runLater( () -> codeArea.insertText( caretPosition, m0.group() ) );
        }
    }
    private void restyleLines(CodeArea codeArea, Set<Integer> lineNumbers) {
        for (int lineNumber : lineNumbers) {
            String line = codeArea.getText(lineNumber);
            IStyler styler = languageTypeProperty.get().getStyler();
            codeArea.setStyleSpans( lineNumber, 0, styler == null
                    ? StyleSpans.singleton(Collections.emptyList(), line.length())
                    : styler.style(line)
            );
        }
    }
    public void addOrReplaceStylesheets(URL stylesheets){
        if(stylesheets == null) return;
        ObservableList<String> stylesheetObservableList = getStylesheets();
        stylesheetObservableList.removeIf(s -> Objects.equals(s,stylesheets.toExternalForm()));
        stylesheetObservableList.add(stylesheets.toExternalForm());
    }

    public boolean getShowLineNumber() {
        return showLineNumberProperty.get();
    }
    public void setShowLineNumber(boolean isShowLineNumber) {
        showLineNumberProperty.set(isShowLineNumber);
    }
    public void setText(String text) {
        this.textProperty.set(text);
    }
    public LanguageType getLanguageType() {
        return languageTypeProperty.get();
    }
    public void setLanguageType(LanguageType languageType) {
        this.languageTypeProperty.set(languageType);
    }
}
