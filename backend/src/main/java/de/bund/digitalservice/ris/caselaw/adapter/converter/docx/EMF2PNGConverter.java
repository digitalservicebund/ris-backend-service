package de.bund.digitalservice.ris.caselaw.adapter.converter.docx;

import java.awt.Dimension;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.ImageConstants;
import org.freehep.graphicsio.ImageGraphics2D;
import org.freehep.graphicsio.emf.EMFInputStream;
import org.freehep.graphicsio.emf.EMFPanel;
import org.freehep.graphicsio.emf.EMFRenderer;

@Slf4j
public class EMF2PNGConverter {
  public static byte[] convertEMF2PNG(byte[] originalBytes, Dimension size) {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      EMFInputStream emf = new EMFInputStream(new ByteArrayInputStream(originalBytes));
      EMFRenderer renderer = new EMFRenderer(emf);

      EMFPanel emfPanel = new EMFPanel();
      emfPanel.setRenderer(renderer);

      size = new Dimension((int) size.getWidth() * 2, (int) size.getHeight() * 2);

      double scaleX = size.getWidth() / emfPanel.getWidth();
      double scaleY = size.getHeight() / emfPanel.getHeight();

      VectorGraphics g = new ImageGraphics2D(outputStream, size, ImageConstants.PNG);
      g.scale(scaleX, scaleY);

      g.startExport();
      emfPanel.print(g);
      g.endExport();

      outputStream.flush();

      return outputStream.toByteArray();
    } catch (Exception ex) {
      log.error("Couldn't convert emf to png", ex);
    }

    return originalBytes;
  }
}
