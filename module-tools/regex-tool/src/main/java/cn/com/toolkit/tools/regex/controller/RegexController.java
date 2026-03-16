package cn.com.toolkit.tools.regex.controller;

import atlantafx.base.theme.Styles;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.CheckComboBox;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RegexController {
    public MenuButton referenceMenuButton;
    public TextField regexTextField;
    public TextArea matchTextArea;
    public TextField replaceTextField;
    public Button replaceButton;
    public TextArea replaceTextArea;
    public TextArea textArea;
    @FXML private CheckComboBox<String> checkComboBox;
    private boolean isGlobal;

    @FXML
    private void initialize(){
        textArea.textProperty().addListener(observable -> processRegex());
        regexTextField.textProperty().addListener(observable -> processRegex());
        checkComboBox.getCheckModel().getCheckedIndices().addListener((ListChangeListener<Integer>) change -> processRegex());


        replaceButton.setOnAction(e -> {
            replaceTextArea.setText("");
            String text = textArea.getText();
            String replaceText = replaceTextField.getText();
            Pattern pattern = generatePattern();
            if(pattern == null || StringUtils.isEmpty(text)) return;
            Matcher matcher = pattern.matcher(text);
            replaceTextArea.setText(isGlobal ? matcher.replaceAll(replaceText) : matcher.replaceFirst(replaceText));
        });



        checkComboBox.getItems().addAll(FXCollections.observableArrayList(
                "全局搜索 -g", "忽略大小写 -i", "多行模式 -m", "包含换行符 -s"
        ));
        checkComboBox.getCheckModel().check(0);
        referenceMenuButton.getItems().setAll(FXCollections.observableArrayList(
                        ". - 除换行符以外的所有字符。",
                        "^ - 字符串开头。",
                        "$ - 字符串结尾。",
                        "\\d,\\w,\\s - 匹配数字、字符、空格。",
                        "\\D,\\W,\\S - 匹配非数字、非字符、非空格。",
                        "[abc] - 匹配 a、b 或 c 中的一个字母。",
                        "[a-z] - 匹配 a 到 z 中的一个字母。",
                        "[^abc] - 匹配除了 a、b 或 c 中的其他字母。",
                        "aa|bb - 匹配 aa 或 bb。",
                        "? - 0 次或 1 次匹配。",
                        "* - 匹配 0 次或多次。",
                        "+ - 匹配 1 次或多次。",
                        "{n} - 匹配 n次。",
                        "{n,} - 匹配 n次以上。",
                        "{m,n} - 最少 m 次，最多 n 次匹配。",
                        "(expr) - 捕获 expr 子模式,以 \\1 使用它。",
                        "(?:expr) - 忽略捕获的子模式。",
                        "(?=expr) - 正向预查模式 expr。",
                        "(?!expr) - 负向预查模式 expr。"
                ).stream().map(MenuItem::new).toList()
        );
    }
    private Pattern generatePattern(){
        isGlobal = false;
        String regex = regexTextField.getText();
        if(StringUtils.isEmpty(regex)) return null;
        String flag = checkComboBox.getCheckModel().getCheckedItems()
                .stream().map(s -> s.charAt(s.length() - 1) + "")
                .collect(Collectors.joining(""));
        if(StringUtils.isNotBlank(flag)){
            if(flag.contains("g")){
                flag = flag.replace("g","");
                isGlobal = true;
            }
            regex = "(?" + flag + ")" + regex;
        }
        return Pattern.compile(regex);
    }
    private void processRegex(){
        matchTextArea.setText("");
        String text = textArea.getText();
        Pattern pattern = generatePattern();
        if(pattern == null || StringUtils.isEmpty(text)) return;
        Matcher matcher = pattern.matcher(text);
        StringBuilder builder  = new StringBuilder();
        while(matcher.find()){
            builder.append(matcher.group()).append("    ");
            if(!isGlobal) break;
        }
        matchTextArea.setText(builder.toString());
    }
}
