package ru.welfegor.imageEditor

import javafx.embed.swing.SwingFXUtils.fromFXImage
import javafx.event.EventHandler
import javafx.application.Application
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.SplitPane
import javafx.scene.image.ImageView
import javafx.scene.layout.AnchorPane
import javafx.stage.FileChooser
import javafx.stage.Stage
import javax.imageio.ImageIO

class MainActivity : SplitPane() {
    @FXML
    private lateinit var floatButton: Button
    @FXML
    private lateinit var intButton: Button
    @FXML
    private lateinit var stringButton: Button
    @FXML
    private lateinit var inputButton: Button
    @FXML
    private lateinit var addImageButton: Button
    @FXML
    private lateinit var addTextButton: Button
    @FXML
    private lateinit var globalOutputImageView: ImageView
    @FXML
    private lateinit var nodeContainer: AnchorPane
    @FXML
    private lateinit var grayButton: Button
    @FXML
    private lateinit var brightnessButton: Button
    @FXML
    private lateinit var sepiaButton: Button
    @FXML
    private lateinit var invertButton: Button
    @FXML
    private lateinit var blurButton: Button
    @FXML
    private lateinit var moveButton: Button
    @FXML
    private lateinit var scaleButton: Button
    @FXML
    private lateinit var rotateButton: Button
    @FXML
    private lateinit var outputButton: Button


    @FXML
    fun initialize() {
        floatButton.onAction = EventHandler {nodeContainer.children.add(FloatClass())}
        intButton.onAction = EventHandler {nodeContainer.children.add(IntClass())}
        stringButton.onAction = EventHandler {nodeContainer.children.add(StringClass())}
        inputButton.onAction = EventHandler {nodeContainer.children.add(InputImage())}
        addImageButton.onAction = EventHandler {nodeContainer.children.add(AddImage())}
        addTextButton.onAction = EventHandler {nodeContainer.children.add(AddText())}
        grayButton.onAction = EventHandler {nodeContainer.children.add(GrayFilterClass())}
        brightnessButton.onAction = EventHandler {nodeContainer.children.add(BrightnessClass())}
        sepiaButton.onAction = EventHandler {nodeContainer.children.add(SepiaFilterClass())}
        invertButton.onAction = EventHandler {nodeContainer.children.add(InvertFilterClass())}
        blurButton.onAction = EventHandler {nodeContainer.children.add(BlurFilterClass())}
        moveButton.onAction = EventHandler {nodeContainer.children.add(TransformMove())}
        scaleButton.onAction = EventHandler {nodeContainer.children.add(TransformScale())}
        rotateButton.onAction = EventHandler {nodeContainer.children.add(TransformRotate())}
        outputButton.onAction = EventHandler {
            val fileChooser = FileChooser()
            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"))
            val file = fileChooser.showSaveDialog(Stage())
            ImageIO.write(fromFXImage(this.globalOutputImageView.image, null), "png", file)
        }
        val temp = EndPointClass()
        temp.imageV = this.globalOutputImageView
        nodeContainer.children.add(temp)
    }

    init {
        val temp = FXMLLoader(javaClass.getResource("ImageEditor.fxml"))
        temp.setRoot(this)
        temp.setController(this)
        temp.load<Any>()
    }

}



class ImageEditor : Application() {
    override fun start(stage: Stage) {
        val mainScene = Scene(MainActivity(), 1280.0, 720.0)
        stage.title = "Image Editor"
        stage.scene = mainScene
        stage.show()
    }
}

fun main() {
    nu.pattern.OpenCV.loadLocally()
    Application.launch(ImageEditor::class.java)
}
