package cn.com.toolkit.tools.qrcode.controller;

import atlantafx.base.controls.ProgressSliderSkin;
import atlantafx.base.util.Animations;
import cn.com.toolkit.framework.core.enums.DatePatternEnum;
import cn.com.toolkit.framework.core.enums.HomeDirectoryEnum;
import cn.com.toolkit.framework.core.support.FXScreenshot;
import cn.com.toolkit.framework.core.util.Notifications;
import cn.com.toolkit.framework.core.util.QRCodeUtil;
import cn.com.toolkit.framework.core.util.ToolKitFXUtil;
import cn.com.toolkit.framework.core.util.ToolKitUtil;
import cn.com.toolkit.tools.qrcode.enums.ErrorCorrectionLevelEnum;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.function.Consumer;

public class QrcodeToolController {
    //截屏快捷键
    private final KeyCombination ALT_SHIFT_A = new KeyCodeCombination(KeyCode.A,KeyCombination.ALT_DOWN,KeyCombination.SHIFT_DOWN);
    private final KeyCombination CTRL_S = new KeyCodeCombination(KeyCode.S,KeyCombination.CONTROL_DOWN);
    private final String extension = "png";
    private final ObservableList<ErrorCorrectionLevelEnum> errorCorrectionLevelObservableList = FXCollections.observableArrayList(
            ErrorCorrectionLevelEnum.L,ErrorCorrectionLevelEnum.M,ErrorCorrectionLevelEnum.Q,ErrorCorrectionLevelEnum.H
    );
    private final ObservableList<String> sizeObservableList = FXCollections.observableArrayList(
      "100","200","250","300","350","400","500","600","800"
    );
    private FXScreenshot screenshot;
    public ColorPicker foregroundColorPicker;
    public ColorPicker backgroundColorPicker;
    public ChoiceBox<ErrorCorrectionLevelEnum> errorCorrectionLevelChoiceBox;
    public ChoiceBox<Integer> marginChoiceBox;
    public Label logoLabel;
    public ImageView logoImageView;
    public Slider logoSlider;
    public ImageView qrcodeImageView;
    public StackPane logoStackPane;
    public Button logoButton;
    public TextArea contentTextArea;
    public ComboBox<String> sizeImageComboBox;
    public Label logoSizeLabel;

    @FXML
    private void initialize(){
        initLogoEvent();
        logoSlider.setSkin(new ProgressSliderSkin(logoSlider));
        errorCorrectionLevelChoiceBox.setItems(errorCorrectionLevelObservableList);
        errorCorrectionLevelChoiceBox.setValue(ErrorCorrectionLevelEnum.H);
        marginChoiceBox.getItems().addAll(1, 2, 3, 4);
        marginChoiceBox.setValue(1);
        sizeImageComboBox.setItems(sizeObservableList);
        sizeImageComboBox.setValue("300");

        Platform.runLater(() -> {
            Stage primaryStage = ToolKitFXUtil.getPrimaryStage();
            screenshot = new FXScreenshot(primaryStage);
            Scene scene = primaryStage.getScene();
            if(scene != null) {
                scene.setOnKeyPressed(event -> {
                    if (ALT_SHIFT_A.match(event)) handleSnapshot(null);
                    if (CTRL_S.match(event)) handleSaveQrcode(null);
                });
            }
        });
    }

    public void handleGenerateQrcode(ActionEvent event) throws Exception {
        String content = contentTextArea.getText();
        if(StringUtils.isEmpty(content)) {
            Notifications.error("请先输入内容!", Pos.TOP_RIGHT);
            return;
        }
        int onColor = getARGB(foregroundColorPicker.getValue());
        int offColor = getARGB(backgroundColorPicker.getValue());
        ErrorCorrectionLevel errorCorrectionLevel = errorCorrectionLevelChoiceBox.getValue().getValue();
        Integer margin = marginChoiceBox.getValue();
        int size = NumberUtils.toInt(sizeImageComboBox.getValue(),300);
        Image logo = logoImageView.getImage();
        double logoPart = logoSlider.getValue();
        QRCodeUtil.QRCodeConfig qrCodeConfig = new QRCodeUtil.QRCodeConfig(content)
                .setColor(onColor,offColor)
                .setLogoBorder(new java.awt.Color(offColor),null)
                .setMargin(margin)
                .setWidth(size)
                .setHeight(size)
                .setErrorLevel(errorCorrectionLevel);
        if(logo != null) qrCodeConfig.setLogo(SwingFXUtils.fromFXImage(logo, null));
        if(logoPart != 0) qrCodeConfig.setLogoPart(logoPart / 100);
        BufferedImage bufferedImage = QRCodeUtil.generateQRCode(qrCodeConfig);
        qrcodeImageView.setImage(convertBufferedImage(bufferedImage));
    }

    public void handleSnapshot(ActionEvent event) {
        contentTextArea.setText(null);
        screenshot.startScreenshot(true,image -> {
            qrcodeImageView.setImage(image);
            String code = null;
            try {
                code = QRCodeUtil.decodeQRCode(SwingFXUtils.fromFXImage(image, null));
                if (StringUtils.isNotEmpty(code)) {
                    contentTextArea.setText(code);
                }
            } catch (Exception e) {
                Notifications.error("未识别到二维码信息!", Pos.TOP_RIGHT);
            }
        });
    }

    public void handleSaveQrcode(ActionEvent event) {
        Image image = qrcodeImageView.getImage();
        if(image == null) return;
        ToolKitFXUtil.openFileChooser(null
                , HomeDirectoryEnum.PICTURES
                ,file -> {
                    try {
                        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                        Notifications.success("保存成功!", Pos.TOP_RIGHT);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                ,fileChooser -> {
                    fileChooser.setTitle("保存图片");
                    String timeStamp = ToolKitUtil.dateToString(LocalDateTime.now(), DatePatternEnum.DATE_TIME_PATTERN_2);
                    fileChooser.setInitialFileName("qrcode_" + timeStamp + ".png");
                }
                ,new FileChooser.ExtensionFilter(extension + "PNG图片 (*." + extension + ")", "*." + extension));
    }
    private void initLogoEvent(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择图片");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + File.separator + "Downloads"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("支持的格式"
                , Arrays.stream(ImageIO.getReaderFormatNames()).map("*"::concat).toList()));
        logoButton.setOnAction(event -> {
            if(logoImageView.getImage() == null && logoButton.isVisible()){
                File imageFile = fileChooser.showOpenDialog(null);
                if(imageFile == null) return;
                try {
                    byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
                    Image image = new Image(imageFile.toURI().toString());
                    logoImageView.setUserData(imageBytes);
                    logoImageView.setImage(image);
                    logoButton.setVisible(false);
                    logoLabel.setVisible(false);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        Consumer<MouseEvent> removeLogoConsumer = mouseEvent -> {
            if(mouseEvent.getButton() != MouseButton.PRIMARY) return;
            if(logoImageView.getImage() != null && logoLabel.isVisible()){
                logoImageView.setUserData(null);
                logoImageView.setImage(null);
                logoButton.setVisible(true);
                logoLabel.setVisible(false);
            }
        };
        logoImageView.setOnMouseClicked(removeLogoConsumer::accept);
        logoLabel.setOnMouseClicked(removeLogoConsumer::accept);

        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(-0.5);
        colorAdjust.setContrast(0.1);
        logoStackPane.setOnMouseEntered(e -> {
            if(logoImageView.getImage() == null) return;
            logoImageView.setEffect(colorAdjust);
            if(logoLabel.isVisible()) return;
            logoLabel.setVisible(true);
            logoLabel.setOpacity(0);
            Timeline in = Animations.fadeIn(logoLabel, Duration.millis(500));
            in.playFromStart();
        });
        logoStackPane.setOnMouseExited(e -> {
            logoImageView.setEffect(null);
            if(logoImageView.getImage() == null) return;
            logoLabel.setVisible(false);
            logoLabel.setOpacity(1);
            Timeline out = Animations.fadeOut(logoLabel, Duration.millis(500));
            out.playFromStart();
        });
        logoSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            logoSizeLabel.setText("图标覆盖区域: "+ newValue.intValue() +"%");
        });
    }
    private Image convertBufferedImage(BufferedImage bufferedImage) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, extension, outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        return new Image(inputStream);
    }
    private int getARGB(Color color) {
        int a = (int) Math.round(color.getOpacity() * 255.0);
        int r = (int) Math.round(color.getRed() * 255.0);
        int g = (int) Math.round(color.getGreen() * 255.0);
        int b = (int) Math.round(color.getBlue() * 255.0);
        // 组合: Alpha << 24 | Red << 16 | Green << 8 | Blue
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
