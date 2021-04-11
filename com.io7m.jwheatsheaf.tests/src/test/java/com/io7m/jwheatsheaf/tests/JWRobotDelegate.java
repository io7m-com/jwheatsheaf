package com.io7m.jwheatsheaf.tests;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextInputControl;
import org.testfx.api.FxRobot;
import org.testfx.service.query.NodeQuery;

import java.util.Objects;

/**
 * Abstracts common unit test functionality with respect to retrieving widgets.
 */

@SuppressWarnings( "unused" )
final class JWRobotDelegate {
  private final FxRobot robot;

  JWRobotDelegate( final FxRobot robot ) {
    this.robot = robot;
  }

  Button getOkButton() {
    return lookup( "#fileChooserOKButton" ).queryButton();
  }

  TextInputControl getNameField() {
    return lookup( "#fileChooserNameField" ).queryTextInputControl();
  }

  Node getSourceList() {
    return query( ".fileChooserSourceList" );
  }

  Node getDirectoryTable() {
    return query( ".fileChooserDirectoryTable" );
  }

  /**
   * @param columnName One of: {@code Size}, {@code Name}, {@code Modified}
   */

  Node getTableColumn( final String columnName ) {
    return query( "#fileChooserTableColumn" + columnName );
  }

  Node query( final String nodeName ) {
    return lookup( nodeName ).query();
  }

  NodeQuery lookup( final String widgetId ) {
    return this.robot.lookup( widgetId );
  }

  TableCell<?, ?> getTableCellFileName( final String text ) {
    return
      this.robot.lookup( n -> n instanceof TableCell )
                .queryAllAs( TableCell.class )
                .stream()
                .filter( cell -> Objects.equals( cell.getText(), text ) )
                .findFirst()
                .orElseThrow( () -> new IllegalStateException(
                  "Unable to locate a '.' directory entry" )
                );
  }
}
