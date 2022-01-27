package ru.welfegor.imageEditor

import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.geometry.Point2D
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.input.ClipboardContent
import javafx.scene.input.TransferMode
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.beans.binding.Bindings
import javafx.beans.binding.When
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.Node
import javafx.scene.input.DataFormat
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import javafx.scene.shape.CubicCurve
import java.util.*

var stateAddLink = DataFormat("linkAdd")
var parentId = DataFormat("parentId")
var stateAddNode = DataFormat("nodeAdd")

open class DraggableNodeController : AnchorPane() {
    @FXML
    lateinit var deleteNode: Button
    @FXML
    lateinit var outputImageView: ImageView
    @FXML
    lateinit var nodeName: Label
    @FXML
    lateinit var inputVBox: VBox
    @FXML
    private lateinit var mainLayout: AnchorPane
    @FXML
    lateinit var nodeContentVBox: VBox
    @FXML
    lateinit var outputVBox: VBox

    @FXML
    fun initialize() {
        id = UUID.randomUUID().toString()
        nodeContentVBox.onDragDetected = EventHandler { mouseEvent ->
            val offset = Point2D(mouseEvent.sceneX - mainLayout.layoutX, mouseEvent.sceneY - mainLayout.layoutY)
            mainLayout.parent.onDragOver = EventHandler { dragEvent ->
                mainLayout.layoutX = dragEvent.sceneX - offset.x
                mainLayout.layoutY = dragEvent.sceneY - offset.y
                dragEvent.acceptTransferModes(*TransferMode.ANY)
                dragEvent.consume()
            }
            mainLayout.parent.onDragDropped = EventHandler { dragEvent ->
                mainLayout.parent.onDragOver = null
                mainLayout.parent.onDragDropped = null
                dragEvent.isDropCompleted = true
                dragEvent.consume()
            }
            val content = ClipboardContent()
            content.putString("node")
            mainLayout.startDragAndDrop(*TransferMode.ANY).setContent(content)
            mouseEvent.consume()
        }

        nodeContentVBox.onDragDone = EventHandler { dragEvent ->
            mainLayout.parent.onDragOver = null
            mainLayout.parent.onDragDropped = null
            dragEvent.consume()
        }
        deleteNode.onAction = EventHandler {
            inputVBox.children.forEach { i ->
                (i as NodeLinkController).deleteAllNodes()
            }
            outputVBox.children.forEach { i ->
                (i as NodeLinkController).deleteAllNodes()
            }
            (this.parent as Pane).children.remove(this)
        }
    }

    init {
        val fxmlLoader = FXMLLoader(javaClass.getResource("Node.fxml"))
        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        fxmlLoader.load<DraggableNodeController>()
    }
}


class NodeLinkController(private val mainParent: DraggableNodeController) : AnchorPane() {
    var linked: Boolean = false
    var state = ""
    var linkClass = "image"
    var outputNode: Node? = null
    var factory: ((NodeLinkController) -> Unit)? = null
    var defactory: ((NodeLinkController) -> Unit)? = null
    var linkedNodes = arrayListOf<NodeLinkController>()
    var sourceMainParent: NodeLinkController? = null
    var superParent: AnchorPane? = null
    private val tempLink: Link = Link()
    private var link: Link? = null

    @FXML
    lateinit var mainLayout: AnchorPane
    @FXML
    lateinit var nodeLinkName: Label
    @FXML
    lateinit var circleItem: Circle

    @FXML
    fun initialize() {
        this.scaleX = 1.2
        this.scaleY = 1.2
        id = UUID.randomUUID().toString()
        circleItem.onDragDetected = EventHandler { mouseEvent ->
            if (link !== null && state === "input") {
                (link!!.parent as Pane).children.remove(link)
                this.sourceMainParent!!.linkedNodes.remove(this)
                defactory?.invoke(this.sourceMainParent!!)
                linked = false
                link = null
            }
            if (superParent == null) superParent = mainParent.parent as AnchorPane
            tempLink.setLocalStartXY(getCircleCenterLocaleXY(mainLayout, circleItem))
            superParent!!.children.add(tempLink)
            tempLink.setStart(Point2D(mainParent.layoutX + tempLink.localStartX, mainParent.layoutY + tempLink.localStartY))
            tempLink.isVisible = true
            superParent!!.onDragOver = EventHandler { dragEvent ->
                if (!tempLink.isVisible) tempLink.isVisible = true
                tempLink.setEnd(Point2D(dragEvent.x, dragEvent.y))
                dragEvent.acceptTransferModes(*TransferMode.ANY)
                dragEvent.consume()
            }
            superParent!!.onDragDropped = EventHandler { dragEvent ->
                superParent!!.onDragOver = null
                superParent!!.onDragDropped = null
                superParent!!.children.remove(tempLink)
                tempLink.isVisible = false
                dragEvent.isDropCompleted = false
                dragEvent.consume()
            }
            val content = ClipboardContent()
            content[stateAddLink] = state
            content[parentId] = mainParent.id
            content[stateAddNode] = linkClass
            circleItem.startDragAndDrop(*TransferMode.ANY).setContent(content)
            mouseEvent.consume()
        }
        circleItem.onDragOver = EventHandler { dragEvent ->
            if (
                dragEvent.dragboard.getContent(stateAddLink) != state &&
                dragEvent.dragboard.getContent(parentId) != mainParent.id &&
                !linked &&
                dragEvent.dragboard.getContent(stateAddNode) == linkClass
            ) {
                dragEvent.acceptTransferModes(*TransferMode.ANY)
            }
            dragEvent.consume()
        }
        circleItem.onDragDropped = EventHandler { dragEvent ->
            if (superParent == null) {
                superParent = mainParent.parent as AnchorPane
            }

            val tmp = Link()
            val sourceMainParent = (dragEvent.gestureSource as Node).parent.parent as NodeLinkController
            this.sourceMainParent = sourceMainParent
            if (state == "input") {
                linked = true
                link = tmp
                factory?.invoke(sourceMainParent)
                sourceMainParent.linkedNodes.add(this)
            }
            if (sourceMainParent.state == "input") {
                sourceMainParent.sourceMainParent = this
                sourceMainParent.factory?.invoke(this)
                sourceMainParent.linked = true
                sourceMainParent.link = tmp
                this.linkedNodes.add(sourceMainParent)
            }
            tmp.setLocalStartXY(getCircleCenterLocaleXY(sourceMainParent, dragEvent.gestureSource as Circle))
            tmp.setLocalEndXY(getCircleCenterLocaleXY(this, circleItem))
            tmp.bindStartEnd(sourceMainParent.mainParent, mainParent)
            superParent!!.children.add(tmp)

            dragEvent.isDropCompleted = true
            dragEvent.consume()
        }
        circleItem.onDragDone = EventHandler { dragEvent ->
            if (superParent == null) {
                superParent = mainParent.parent as AnchorPane
            }
            superParent!!.onDragOver = null
            superParent!!.onDragDropped = null
            superParent!!.children.remove(tempLink)
            dragEvent.consume()
        }
    }

    fun deleteAllNodes() {
        if (this.state == "output") {
            linkedNodes.forEach { i ->
                i.defactory?.invoke(this)
                (i.link!!.parent as Pane).children.remove(i.link)
                i.linked = false
                i.link = null
            }
            linkedNodes.clear()
        } else if (this.state == "input") {
            if (this.defactory !== null && this.sourceMainParent !== null) {
                this.defactory?.invoke(this.sourceMainParent!!)
            }
            if (this.sourceMainParent !== null){
                this.sourceMainParent!!.linkedNodes.remove(this)
            }
            if (this.link !== null) {
                (this.link!!.parent as Pane).children.remove(this.link!!)
            }
            this.linked = false
            this.link = null
        }
    }

    private fun getCircleCenterLocaleXY(mainLayout: Node, circleItem: Circle): Point2D {
        return mainLayout.parent.parent.localToParent(
            mainLayout.parent.localToParent(
                mainLayout.localToParent(circleItem.parent.localToParent(circleItem.localToParent(circleItem.centerX, circleItem.centerY)))
            )
        )
    }

    init {
        val fxmlLoader = FXMLLoader(javaClass.getResource("Connector.fxml"))
        fxmlLoader.setRoot(this)
        fxmlLoader.setController(this)
        fxmlLoader.load<Any>()
    }
}

// Example
class Link : CubicCurve() {
    var localStartX = 0.0
    var localStartY = 0.0
    var localEndX = 0.0
    var localEndY = 0.0
    private val offsetX = SimpleDoubleProperty()
    private val offsetY = SimpleDoubleProperty()
    private val offsetDirX1 = SimpleDoubleProperty()
    private val offsetDirX2 = SimpleDoubleProperty()
    private val offsetDirY1 = SimpleDoubleProperty()
    private val offsetDirY2 = SimpleDoubleProperty()
    private fun initialize() {

        strokeWidth = 3.0
        fill = Color.TRANSPARENT
        stroke = Color.BLACK
        isVisible = true
        isDisable = false
        offsetX.set(100.0)
        offsetY.set(50.0)
        offsetDirX1.bind(
            When(startXProperty().greaterThan(endXProperty())).then(-1.0).otherwise(1.0)
        )
        offsetDirX2.bind(
            When(startXProperty().greaterThan(endXProperty())).then(1.0).otherwise(-1.0)
        )
        controlX1Property().bind(Bindings.add(startXProperty(), offsetX.multiply(offsetDirX1)))
        controlX2Property().bind(Bindings.add(endXProperty(), offsetX.multiply(offsetDirX2)))
        controlY1Property().bind(Bindings.add(startYProperty(), offsetY.multiply(offsetDirY1)))
        controlY2Property().bind(Bindings.add(endYProperty(), offsetY.multiply(offsetDirY2)))
    }

    fun setLocalStartXY(p: Point2D) {
        localStartX = p.x
        localStartY = p.y
    }

    fun setLocalEndXY(p: Point2D) {
        localEndX = p.x
        localEndY = p.y
    }

    fun setStart(p: Point2D) {
        startX = p.x
        startY = p.y
    }

    fun setEnd(p: Point2D) {
        endX = p.x
        endY = p.y
    }

    fun bindStartEnd(source1: Node, source2: Node) {
        startXProperty().bind(Bindings.add(source1.layoutXProperty(), localStartX))
        startYProperty().bind(Bindings.add(source1.layoutYProperty(), localStartY))
        endXProperty().bind(Bindings.add(source2.layoutXProperty(), localEndX))
        endYProperty().bind(Bindings.add(source2.layoutYProperty(), localEndY))
    }

    init {
        initialize()
    }
}
