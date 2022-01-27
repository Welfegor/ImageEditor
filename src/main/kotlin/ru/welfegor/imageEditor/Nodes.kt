package ru.welfegor.imageEditor

import javafx.beans.value.ChangeListener
import javafx.embed.swing.SwingFXUtils.fromFXImage
import javafx.embed.swing.SwingFXUtils.toFXImage
import javafx.event.EventHandler
import javafx.scene.SnapshotParameters
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.Stage
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import javax.imageio.ImageIO


class FloatClass : DraggableNodeController() {
    private val output = TextField("0.0")

    init {
        output.textProperty().addListener { _, oldValue, newValue ->
            try {
                output.text = newValue.toDouble().toString()
            } catch (_: Exception) {
                if (newValue !== "") {
                    output.text = oldValue
                }
            }
        }
        this.nodeContentVBox.children.remove(this.outputImageView)
        this.nodeName.text = "Float"
        this.nodeContentVBox.children.add(output)
        val tmp = NodeLinkController(this)
        tmp.nodeLinkName.text = "Out"
        tmp.state = "output"
        tmp.linkClass = "float"
        tmp.outputNode = output
        this.outputVBox.children.add(tmp)
    }
}

class IntClass : DraggableNodeController() {
    private val output = TextField("0")

    init {
        output.textProperty().addListener { _, oldValue, newValue ->
            try {
                output.text = newValue.toInt().toString()
            } catch (_: Exception) {
                if (newValue !== "") {
                    output.text = oldValue
                }
            }
        }
        this.nodeContentVBox.children.remove(this.outputImageView)
        this.nodeName.text = "Integer"
        this.nodeContentVBox.children.add(output)
        val tmp = NodeLinkController(this)
        tmp.nodeLinkName.text = "Out"
        tmp.state = "output"
        tmp.linkClass = "int"
        tmp.outputNode = output
        this.outputVBox.children.add(tmp)
    }
}

class StringClass : DraggableNodeController() {
    private val output = TextField("")

    init {
        this.nodeContentVBox.children.remove(this.outputImageView)
        this.nodeName.text = "String"
        this.nodeContentVBox.children.add(output)
        val tmp = NodeLinkController(this)
        tmp.nodeLinkName.text = "Out"
        tmp.state = "output"
        tmp.linkClass = "string"
        tmp.outputNode = output
        this.outputVBox.children.add(tmp)
    }
}

class InputImage : DraggableNodeController() {
    private val fileButton = Button("Choose image")
    private var image: Image? = null
    private val link = NodeLinkController(this)

    init {
        this.nodeName.text = "Input Image"
        fileButton.onAction = EventHandler {
            val fileChooser = FileChooser()
            fileChooser.extensionFilters.add(FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"))
            val file = fileChooser.showOpenDialog(Stage())
            image = toFXImage(ImageIO.read(file), null)
            if (image !== null) {
                this.outputImageView.image = image
            }
        }
        this.nodeContentVBox.children.add(fileButton)
        link.linkClass = "image"
        link.state = "output"
        link.outputNode = this.outputImageView
        link.nodeLinkName.text = "Out"
        this.outputVBox.children.add(link)
    }
}

class AddText : DraggableNodeController() {
    private var image: Image? = null
    private var textX = 0
    private var textY = 0
    private var text = ""
    private val inputImage = NodeLinkController(this)
    private val inputIntX = NodeLinkController(this)
    private val inputIntY = NodeLinkController(this)
    private val inputStringText = NodeLinkController(this)
    private val outputImage = NodeLinkController(this)

    private val inputImageListener = ChangeListener<Image> { _, _, newValue ->
        this.image = newValue
        this.update()
    }
    private val inputIntXListener = ChangeListener<String> { _, _, newValue ->
        if (newValue == "") {
            this.textX = 0
        } else {
            this.textX = newValue.toInt()
        }
        this.update()
    }
    private val inputIntYListener = ChangeListener<String> { _, _, newValue ->
        if (newValue == "") {
            this.textY = 0
        } else {
            this.textY = newValue.toInt()
        }
        this.update()
    }
    private val inputStringTextListener = ChangeListener<String> { _, _, newValue ->
        this.text = newValue
        this.update()
    }

    private fun update() {
        var width = 0.0
        var height = 0.0
        if (this.image !== null) width = this.image!!.width
        if (this.image !== null) height = this.image!!.height
        val tmp = Canvas(width, height)
        val tmp2 = tmp.graphicsContext2D
        tmp2.drawImage(this.image, 0.0, 0.0)
        tmp2.fillText(this.text, this.textX.toDouble(), this.textY.toDouble())
        val snapParam = SnapshotParameters()
        snapParam.fill = Color.TRANSPARENT
        this.outputImageView.image = tmp.snapshot(snapParam, null)
    }

    init {
        this.scaleX = 1.5
        this.scaleY = 1.5

        inputImage.factory = fun(source: NodeLinkController) {
            this.image = (source.outputNode as ImageView).image
            (source.outputNode as ImageView).imageProperty().addListener(inputImageListener)
        }
        inputImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputImageListener)
        }
        inputImage.nodeLinkName.text = "Img"
        inputImage.state = "input"
        inputImage.linkClass = "image"
        this.inputVBox.children.add(inputImage)
        inputIntX.factory = fun(source: NodeLinkController) {
            this.textX = (source.outputNode as TextField).text.toInt()
            (source.outputNode as TextField).textProperty().addListener(inputIntXListener)
        }
        inputIntX.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputIntXListener)
        }
        inputIntX.nodeLinkName.text = "X"
        inputIntX.state = "input"
        inputIntX.linkClass = "int"
        this.inputVBox.children.add(inputIntX)
        inputIntY.factory = fun(source: NodeLinkController) {
            this.textY = (source.outputNode as TextField).text.toInt()
            (source.outputNode as TextField).textProperty().addListener(inputIntYListener)
        }
        inputIntY.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputIntYListener)
        }
        inputIntY.nodeLinkName.text = "Y"
        inputIntY.state = "input"
        inputIntY.linkClass = "int"
        this.inputVBox.children.add(inputIntY)
        inputStringText.factory = fun(source: NodeLinkController) {
            this.text = (source.outputNode as TextField).text
            (source.outputNode as TextField).textProperty().addListener(inputStringTextListener)
        }
        inputStringText.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputStringTextListener)
        }
        inputStringText.nodeLinkName.text = "Text"
        inputStringText.state = "input"
        inputStringText.linkClass = "string"
        this.inputVBox.children.add(inputStringText)

        this.outputImage.outputNode = this.outputImageView
        this.outputImage.state = "output"
        this.outputImage.linkClass = "image"
        this.outputImage.nodeLinkName.text = "Out"
        this.outputVBox.children.add(this.outputImage)
    }
}

class AddImage : DraggableNodeController() {
    private var image: Image? = null
    private var imageX = 0.0
    private var imageY = 0.0
    private var addImage: Image? = null
    private val inputImage = NodeLinkController(this)
    private val inputIntX = NodeLinkController(this)
    private val inputIntY = NodeLinkController(this)
    private val inputAddImage = NodeLinkController(this)
    private val outputImage = NodeLinkController(this)

    private val inputImageListener = ChangeListener<Image> { _, _, newValue ->
        this.image = newValue
        this.update()
    }
    private val inputIntXListener = ChangeListener<String> { _, _, newValue ->
        if (newValue == "") {
            this.imageX = 0.0
        } else {
            this.imageX = newValue.toDouble()
        }
        this.update()
    }
    private val inputIntYListener = ChangeListener<String> { _, _, newValue ->
        if (newValue == "") {
            this.imageY = 0.0
        } else {
            this.imageY = newValue.toDouble()
        }
        this.update()
    }
    private val inputAddImageListener = ChangeListener<Image> { _, _, newValue ->
        this.addImage = newValue
        this.update()
    }

    private fun update() {
        var width = 0.0
        var height = 0.0
        if (this.image !== null) width = this.image!!.width
        if (this.image !== null) height = this.image!!.height
        val tmp = Canvas(width, height)
        val tmp2 = tmp.graphicsContext2D
        tmp2.drawImage(this.image, 0.0, 0.0)
        tmp2.drawImage(this.addImage, this.imageX, this.imageY)
        val snapParam = SnapshotParameters()
        snapParam.fill = Color.TRANSPARENT
        this.outputImageView.image = tmp.snapshot(snapParam, null)
    }

    init {
        this.scaleX = 1.5
        this.scaleY = 1.5
        this.nodeName.text = "Add image"
        inputImage.factory = fun(source: NodeLinkController) {
            this.image = (source.outputNode as ImageView).image
            this.update()
            (source.outputNode as ImageView).imageProperty().addListener(inputImageListener)
        }
        inputImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputImageListener)
        }
        inputImage.nodeLinkName.text = "Img"
        inputImage.state = "input"
        inputImage.linkClass = "image"
        this.inputVBox.children.add(inputImage)
        inputIntX.factory = fun(source: NodeLinkController) {
            this.imageX = (source.outputNode as TextField).text.toDouble()
            this.update()
            (source.outputNode as TextField).textProperty().addListener(inputIntXListener)
        }
        inputIntX.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputIntXListener)
        }
        inputIntX.nodeLinkName.text = "X"
        inputIntX.state = "input"
        inputIntX.linkClass = "int"
        this.inputVBox.children.add(inputIntX)
        inputIntY.factory = fun(source: NodeLinkController) {
            this.imageY = (source.outputNode as TextField).text.toDouble()
            this.update()
            (source.outputNode as TextField).textProperty().addListener(inputIntYListener)
        }
        inputIntY.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputIntYListener)
        }
        inputIntY.nodeLinkName.text = "Y"
        inputIntY.state = "input"
        inputIntY.linkClass = "int"
        this.inputVBox.children.add(inputIntY)
        inputAddImage.factory = fun(source: NodeLinkController) {
            this.addImage = (source.outputNode as ImageView).image
            this.update()
            (source.outputNode as ImageView).imageProperty().addListener(inputAddImageListener)
        }
        inputAddImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputAddImageListener)
        }
        inputAddImage.nodeLinkName.text = "AddImg"
        inputAddImage.state = "input"
        inputAddImage.linkClass = "image"
        this.inputVBox.children.add(inputAddImage)
        this.outputImage.outputNode = this.outputImageView
        this.outputImage.state = "output"
        this.outputImage.linkClass = "image"
        this.outputImage.nodeLinkName.text = "Out"
        this.outputVBox.children.add(this.outputImage)
    }
}

class GrayFilterClass : DraggableNodeController() {
    private var image: Image? = null
    private val inputImage = NodeLinkController(this)
    private val outputImage = NodeLinkController(this)
    private val inputImageListener = ChangeListener<Image> { _, _, newValue ->
        this.image = newValue
        this.update()
    }

    private fun update() {
        if (this.image !== null) {
            val mat = img_to_mat(this.image!!)
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2GRAY)
            this.outputImageView.image = mat_to_image(mat)
        }
    }

    init {
        this.nodeName.text = "Gray Filter"
        inputImage.factory = fun(source: NodeLinkController) {
            this.image = (source.outputNode as ImageView).image
            this.update()
            (source.outputNode as ImageView).imageProperty().addListener(inputImageListener)
        }
        inputImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputImageListener)
        }
        inputImage.nodeLinkName.text = "Img"
        inputImage.state = "input"
        inputImage.linkClass = "image"
        this.inputVBox.children.add(inputImage)
        this.outputImage.outputNode = this.outputImageView
        this.outputImage.state = "output"
        this.outputImage.linkClass = "image"
        this.outputImage.nodeLinkName.text = "Out"
        this.outputVBox.children.add(this.outputImage)
    }
}

class BrightnessClass : DraggableNodeController() {
    private var image: Image? = null
    private var level = 0.0
    private val inputImage = NodeLinkController(this)
    private val inputLevel = NodeLinkController(this)
    private val outputImage = NodeLinkController(this)
    private val inputImageListener = ChangeListener<Image> { _, _, newValue ->
        this.image = newValue
        this.update()
    }
    private val inputLevelListener = ChangeListener<String> { _, _, newValue ->
        if (newValue == "") {
            this.level = 0.0
        } else {
            this.level = newValue.toDouble()
        }
        this.update()
    }

    private fun update() {
        if (this.image !== null) {
            val mat = img_to_mat(this.image!!)
            mat.convertTo(mat, -1, this.level)
            this.outputImageView.image = mat_to_image(mat)
        }
    }

    init {
        this.nodeName.text = "Brightness Filter"
        inputImage.factory = fun(source: NodeLinkController) {
            this.image = (source.outputNode as ImageView).image
            this.update()
            (source.outputNode as ImageView).imageProperty().addListener(inputImageListener)
        }
        inputImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputImageListener)
        }
        inputImage.nodeLinkName.text = "Img"
        inputImage.state = "input"
        inputImage.linkClass = "image"
        this.inputVBox.children.add(inputImage)
        inputLevel.factory = fun(source: NodeLinkController) {
            this.level = (source.outputNode as TextField).text.toDouble()
            this.update()
            (source.outputNode as TextField).textProperty().addListener(inputLevelListener)
        }
        inputLevel.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputLevelListener)
        }
        inputLevel.nodeLinkName.text = "Bright"
        inputLevel.state = "input"
        inputLevel.linkClass = "float"
        this.inputVBox.children.add(inputLevel)
        this.outputImage.outputNode = this.outputImageView
        this.outputImage.state = "output"
        this.outputImage.linkClass = "image"
        this.outputImage.nodeLinkName.text = "Out"
        this.outputVBox.children.add(this.outputImage)
    }
}

class SepiaFilterClass : DraggableNodeController() {
    private var image: Image? = null
    private val inputImage = NodeLinkController(this)
    private val outputImage = NodeLinkController(this)
    private val inputImageListener = ChangeListener<Image> { _, _, newValue ->
        this.image = newValue
        this.update()
    }

    private fun update() {
        if (this.image !== null) {
            val img = fromFXImage(this.image!!, null)
            for (y in 0 until img.height) {
                for (x in 0 until img.width) {
                    val pixel: Int = img.getRGB(x, y)
                    var color = java.awt.Color(pixel, true)
                    var red = color.red
                    var green = color.green
                    var blue = color.blue
                    val avg = (red + green + blue) / 3
                    val depth = 20
                    val intensity = 30
                    red = avg + depth * 2
                    green = avg + depth
                    blue = avg - intensity
                    if (red > 255) red = 255
                    if (green > 255) green = 255
                    if (blue > 255) blue = 255
                    if (blue < 0) blue = 0
                    color = java.awt.Color(red.toFloat() / 255, green.toFloat() / 255, blue.toFloat() / 255)
                    img.setRGB(x, y, color.rgb)
                }
            }
            this.outputImageView.image = toFXImage(img, null)
        }
    }

    init {
        this.nodeName.text = "Sepia Filter"
        inputImage.factory = fun(source: NodeLinkController) {
            this.image = (source.outputNode as ImageView).image
            this.update()
            (source.outputNode as ImageView).imageProperty().addListener(inputImageListener)
        }
        inputImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputImageListener)
        }
        inputImage.nodeLinkName.text = "Img"
        inputImage.state = "input"
        inputImage.linkClass = "image"
        this.inputVBox.children.add(inputImage)
        this.outputImage.outputNode = this.outputImageView
        this.outputImage.state = "output"
        this.outputImage.linkClass = "image"
        this.outputImage.nodeLinkName.text = "Out"
        this.outputVBox.children.add(this.outputImage)
    }
}

class InvertFilterClass : DraggableNodeController() {
    private var image: Image? = null
    private val inputImage = NodeLinkController(this)
    private val outputImage = NodeLinkController(this)
    private val inputImageListener = ChangeListener<Image> { _, _, newValue ->
        this.image = newValue
        this.update()
    }

    private fun update() {
        if (this.image !== null) {
            val img = fromFXImage(this.image!!, null)
            for (y in 0 until img.height) {
                for (x in 0 until img.width) {
                    val pixel: Int = img.getRGB(x, y)
                    var color = java.awt.Color(pixel, true)
                    color = java.awt.Color(255 - color.red, 255 - color.green, 255 - color.blue)
                    img.setRGB(x, y, color.rgb)
                }
            }
            this.outputImageView.image = toFXImage(img, null)
        }
    }

    init {
        this.nodeName.text = "Invert Filter"
        inputImage.factory = fun(source: NodeLinkController) {
            this.image = (source.outputNode as ImageView).image
            this.update()
            (source.outputNode as ImageView).imageProperty().addListener(inputImageListener)
        }
        inputImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputImageListener)
        }
        inputImage.nodeLinkName.text = "Img"
        inputImage.state = "input"
        inputImage.linkClass = "image"
        this.inputVBox.children.add(inputImage)
        this.outputImage.outputNode = this.outputImageView
        this.outputImage.state = "output"
        this.outputImage.linkClass = "image"
        this.outputImage.nodeLinkName.text = "Out"
        this.outputVBox.children.add(this.outputImage)
    }
}

class BlurFilterClass : DraggableNodeController() {
    private var image: Image? = null
    private var kernelSize = 1
    private val inputImage = NodeLinkController(this)
    private val inputLevel = NodeLinkController(this)
    private val outputImage = NodeLinkController(this)
    private val inputImageListener = ChangeListener<Image> { _, _, newValue ->
        this.image = newValue
        this.update()
    }
    private val inputLevelListener = ChangeListener<String> { _, _, newValue ->
        if (newValue == "") {
            this.kernelSize = 0
        } else {
            this.kernelSize = newValue.toInt()
        }
        this.update()
    }

    private fun update() {
        if (this.image !== null) {
            val mat = img_to_mat(this.image!!)
            try {
                Imgproc.GaussianBlur(mat, mat, Size(this.kernelSize.toDouble(), this.kernelSize.toDouble()), 0.0)
            } catch (_: Exception) {
            }
            this.outputImageView.image = mat_to_image(mat)
        }
    }

    init {
        this.nodeName.text = "Blur Filter"
        inputImage.factory = fun(source: NodeLinkController) {
            this.image = (source.outputNode as ImageView).image
            this.update()
            (source.outputNode as ImageView).imageProperty().addListener(inputImageListener)
        }
        inputImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputImageListener)
        }
        inputImage.nodeLinkName.text = "Img"
        inputImage.state = "input"
        inputImage.linkClass = "image"
        this.inputVBox.children.add(inputImage)
        inputLevel.factory = fun(source: NodeLinkController) {
            this.kernelSize = (source.outputNode as TextField).text.toInt()
            this.update()
            (source.outputNode as TextField).textProperty().addListener(inputLevelListener)
        }
        inputLevel.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputLevelListener)
        }
        inputLevel.nodeLinkName.text = "KernelSize"
        inputLevel.state = "input"
        inputLevel.linkClass = "int"
        this.inputVBox.children.add(inputLevel)
        this.outputImage.outputNode = this.outputImageView
        this.outputImage.state = "output"
        this.outputImage.linkClass = "image"
        this.outputImage.nodeLinkName.text = "Out"
        this.outputVBox.children.add(this.outputImage)
    }
}


class EndPointClass : DraggableNodeController() {
    private var image: Image? = null
    var imageV: ImageView? = null
    private val inputImage = NodeLinkController(this)
    private val inputImageListener = ChangeListener<Image> { _, _, newValue ->
        this.image = newValue
        this.update()
    }

    private fun update() {
        if (this.image !== null) {
            imageV!!.image = this.image
            imageV!!.fitHeight = this.image!!.height
            imageV!!.fitWidth = this.image!!.width
        }
    }

    init {
        (this.deleteNode.parent as Pane).children.remove(this.deleteNode)
        (this.outputImageView.parent as Pane).children.remove(this.outputImageView)
        this.nodeName.text = "Output image"
        inputImage.factory = fun(source: NodeLinkController) {
            this.image = (source.outputNode as ImageView).image
            this.update()
            (source.outputNode as ImageView).imageProperty().addListener(inputImageListener)
        }
        inputImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputImageListener)
        }
        inputImage.nodeLinkName.text = "Img"
        inputImage.state = "input"
        inputImage.linkClass = "image"
        this.inputVBox.children.add(inputImage)
    }
}

class TransformScale : DraggableNodeController() {
    private var image: Image? = null
    private var imageScaleX = 1.0
    private var imageScaleY = 1.0
    private val inputImage = NodeLinkController(this)
    private val inputFloatX = NodeLinkController(this)
    private val inputFloatY = NodeLinkController(this)
    private val outputImage = NodeLinkController(this)
    private val inputImageListener = ChangeListener<Image> { _, _, newValue ->
        this.image = newValue
        this.update()
    }
    private val inputIntXListener = ChangeListener<String> { _, _, newValue ->
        if (newValue == "") {
            this.imageScaleX = 1.0
        } else {
            this.imageScaleX = newValue.toDouble()
        }
        this.update()
    }
    private val inputIntYListener = ChangeListener<String> { _, _, newValue ->
        if (newValue == "") {
            this.imageScaleY = 1.0
        } else {
            this.imageScaleY = newValue.toDouble()
        }
        this.update()
    }

    private fun update() {
        if (this.image !== null) {
            val mat = img_to_mat(this.image!!)

            try {
                val mat2 = Mat()
                Imgproc.resize(mat, mat2, Size(0.0, 0.0), this.imageScaleX, this.imageScaleY)
                this.outputImageView.image = mat_to_image(mat2)
            } catch (e: Exception) {
                this.outputImageView.image = mat_to_image(mat)
                println(e)
            }

        }
    }

    init {
        this.nodeName.text = "Transform Scale"
        inputImage.factory = fun(source: NodeLinkController) {
            this.image = (source.outputNode as ImageView).image
            this.update()
            (source.outputNode as ImageView).imageProperty().addListener(inputImageListener)
        }
        inputImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputImageListener)
        }
        inputImage.nodeLinkName.text = "Img"
        inputImage.state = "input"
        inputImage.linkClass = "image"
        this.inputVBox.children.add(inputImage)
        inputFloatX.factory = fun(source: NodeLinkController) {
            this.imageScaleX = (source.outputNode as TextField).text.toDouble()
            this.update()
            (source.outputNode as TextField).textProperty().addListener(inputIntXListener)
        }
        inputFloatX.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputIntXListener)
        }
        inputFloatX.nodeLinkName.text = "X"
        inputFloatX.state = "input"
        inputFloatX.linkClass = "float"
        this.inputVBox.children.add(inputFloatX)
        inputFloatY.factory = fun(source: NodeLinkController) {
            this.imageScaleY = (source.outputNode as TextField).text.toDouble()
            this.update()
            (source.outputNode as TextField).textProperty().addListener(inputIntYListener)
        }
        inputFloatY.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputIntYListener)
        }
        inputFloatY.nodeLinkName.text = "Y"
        inputFloatY.state = "input"
        inputFloatY.linkClass = "float"
        this.inputVBox.children.add(inputFloatY)
        this.outputImage.outputNode = this.outputImageView
        this.outputImage.state = "output"
        this.outputImage.linkClass = "image"
        this.outputImage.nodeLinkName.text = "Out"
        this.outputVBox.children.add(this.outputImage)
    }
}

class TransformMove : DraggableNodeController() {
    private var image: Image? = null
    private var imageX = 0.0
    private var imageY = 0.0
    private val inputImage = NodeLinkController(this)
    private val inputFloatX = NodeLinkController(this)
    private val inputFloatY = NodeLinkController(this)
    private val outputImage = NodeLinkController(this)
    private val inputImageListener = ChangeListener<Image> { _, _, newValue ->
        this.image = newValue
        this.update()
    }
    private val inputIntXListener = ChangeListener<String> { _, _, newValue ->
        if (newValue == "") {
            this.imageX = 1.0
        } else {
            this.imageX = newValue.toDouble()
        }
        this.update()
    }
    private val inputIntYListener = ChangeListener<String> { _, _, newValue ->
        if (newValue == "") {
            this.imageY = 1.0
        } else {
            this.imageY = newValue.toDouble()
        }
        this.update()
    }

    private fun update() {
        if (this.image !== null) {
            val tmp = Canvas(this.image!!.width + this.imageX, this.image!!.height + this.imageY)
            val tmp2 = tmp.graphicsContext2D
            tmp2.drawImage(this.image, this.imageX, this.imageY)
            val tmp3 = SnapshotParameters()
            tmp3.fill = Color.WHITE
            this.outputImageView.image = tmp.snapshot(tmp3, null)
        }
    }

    init {
        this.nodeName.text = "Transform move"
        inputImage.factory = fun(source: NodeLinkController) {
            this.image = (source.outputNode as ImageView).image
            this.update()
            (source.outputNode as ImageView).imageProperty().addListener(inputImageListener)
        }
        inputImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputImageListener)
        }
        inputImage.nodeLinkName.text = "Img"
        inputImage.state = "input"
        inputImage.linkClass = "image"
        this.inputVBox.children.add(inputImage)
        inputFloatX.factory = fun(source: NodeLinkController) {
            this.imageX = (source.outputNode as TextField).text.toDouble()
            this.update()
            (source.outputNode as TextField).textProperty().addListener(inputIntXListener)
        }
        inputFloatX.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputIntXListener)
        }
        inputFloatX.nodeLinkName.text = "X"
        inputFloatX.state = "input"
        inputFloatX.linkClass = "float"
        this.inputVBox.children.add(inputFloatX)
        inputFloatY.factory = fun(source: NodeLinkController) {
            this.imageY = (source.outputNode as TextField).text.toDouble()
            this.update()
            (source.outputNode as TextField).textProperty().addListener(inputIntYListener)
        }
        inputFloatY.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputIntYListener)
        }
        inputFloatY.nodeLinkName.text = "Y"
        inputFloatY.state = "input"
        inputFloatY.linkClass = "float"
        this.inputVBox.children.add(inputFloatY)
        this.outputImage.outputNode = this.outputImageView
        this.outputImage.state = "output"
        this.outputImage.linkClass = "image"
        this.outputImage.nodeLinkName.text = "Out"
        this.outputVBox.children.add(this.outputImage)
    }
}

class TransformRotate : DraggableNodeController() {
    private var image: Image? = null
    private var rad = 0.0
    private val inputImage = NodeLinkController(this)
    private val inputRad = NodeLinkController(this)
    private val outputImage = NodeLinkController(this)
    private val inputImageListener = ChangeListener<Image> { _, _, newValue ->
        this.image = newValue
        this.update()
    }
    private val inputIntXListener = ChangeListener<String> { _, _, newValue ->
        if (newValue == "") {
            this.rad = 0.0
        } else {
            this.rad = newValue.toDouble()
        }
        this.update()
    }

    private fun update() {
        if (this.image !== null) {
            val tmp = Canvas(this.image!!.width, this.image!!.height)
            tmp.rotate = this.rad * 180 / Math.PI
            val tmp2 = tmp.graphicsContext2D
            tmp2.drawImage(this.image, 0.0, 0.0)
            val tmp3 = SnapshotParameters()
            tmp3.fill = Color.WHITE
            this.outputImageView.image = tmp.snapshot(tmp3, null)
        }
    }

    init {
        this.nodeName.text = "Transform Rotate"
        inputImage.factory = fun(source: NodeLinkController) {
            this.image = (source.outputNode as ImageView).image
            this.update()
            (source.outputNode as ImageView).imageProperty().addListener(inputImageListener)
        }
        inputImage.defactory = fun(source: NodeLinkController) {
            (source.outputNode as ImageView).imageProperty().removeListener(inputImageListener)
        }
        inputImage.nodeLinkName.text = "Img"
        inputImage.state = "input"
        inputImage.linkClass = "image"
        this.inputVBox.children.add(inputImage)
        inputRad.factory = fun(source: NodeLinkController) {
            this.rad = (source.outputNode as TextField).text.toDouble()
            this.update()
            (source.outputNode as TextField).textProperty().addListener(inputIntXListener)
        }
        inputRad.defactory = fun(source: NodeLinkController) {
            (source.outputNode as TextField).textProperty().removeListener(inputIntXListener)
        }
        inputRad.nodeLinkName.text = "Rad"
        inputRad.state = "input"
        inputRad.linkClass = "float"
        this.inputVBox.children.add(inputRad)
        this.outputImage.outputNode = this.outputImageView
        this.outputImage.state = "output"
        this.outputImage.linkClass = "image"
        this.outputImage.nodeLinkName.text = "Out"
        this.outputVBox.children.add(this.outputImage)
    }
}

fun img_to_mat(img: Image): Mat {
    val width = img.width.toInt()
    val height = img.height.toInt()
    val bufImg = fromFXImage(img, null)
    val convertedImage = BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)
    convertedImage.graphics.drawImage(bufImg, 0, 0, null)
    val mat = Mat(height, width, CvType.CV_8UC3)
    mat.put(0, 0, (convertedImage.raster.dataBuffer as DataBufferByte).data)
    return mat
}

fun mat_to_image(frame: Mat): Image? {
    return try {
        toFXImage(matToBufferedImage(frame), null)
    } catch (e: java.lang.Exception) {
        System.err.println("error: $e")
        null
    }
}

private fun matToBufferedImage(original: Mat): BufferedImage {
    var image: BufferedImage? = null
    val width = original.width()
    val height = original.height()
    val channels = original.channels()
    val sourcePixels = ByteArray(width * height * channels)
    original[0, 0, sourcePixels]
    image = if (original.channels() > 1) {
        BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR)
    } else {
        BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
    }
    val targetPixels = (image.raster.dataBuffer as DataBufferByte).data
    System.arraycopy(sourcePixels, 0, targetPixels, 0, sourcePixels.size)
    return image
}