package de.presti.ree6.utils.apis;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import de.presti.ree6.main.Main;

import java.util.ArrayList;
import java.util.List;

/**
 * Classed used to handle request related to the Google Vision AI API.
 * If you want to use this feature check out <a href="https://github.com/googleapis/java-vision#prerequisites">this</a>
 */
public class GoogleVisionAPI {

    /**
     * Constructor should not be called, since it is a utility class that doesn't need an instance.
     *
     * @throws IllegalStateException it is a utility class.
     */
    private GoogleVisionAPI() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get all text from the given Image.
     *
     * @param imageBytes the Image-Bytes.
     * @return an {@link String[]} with all text from the Image.
     */
    public static String[] retrieveTextFromImage(byte[] imageBytes) {
        List<AnnotateImageRequest> requests = new ArrayList<>();

        ByteString imgBytes = ByteString.copyFrom(imageBytes);

        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
        requests.add(request);

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.
        String[] texts = new String[0];
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    Main.getInstance().getAnalyticsLogger().error("Could not retrieve text from image: {}", res.getError().getMessage());
                    return new String[0];
                }

                // For full list of available annotations, see http://g.co/cloud/vision/docs
                texts = res.getFullTextAnnotation().getText().split("\n");
            }
        } catch (Exception ignored) {
        }

        return texts;
    }

    /**
     * Get all text from the given Image.
     *
     * @param fileUrl the Image url.
     * @return an {@link String[]} with all text from the Image.
     */
    public static String[] retrieveTextFromImage(String fileUrl) {
        List<AnnotateImageRequest> requests = new ArrayList<>();

        Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(Image.newBuilder().setSource(ImageSource.newBuilder().setGcsImageUri(fileUrl).build()).build()).build();
        requests.add(request);

        // Initialize client that will be used to send requests. This client only needs to be created
        // once, and can be reused for multiple requests. After completing all of your requests, call
        // the "close" method on the client to safely clean up any remaining background resources.

        String[] texts = new String[0];
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responses = response.getResponsesList();

            for (AnnotateImageResponse res : responses) {
                if (res.hasError()) {
                    Main.getInstance().getAnalyticsLogger().error("Could not retrieve text from image: {}", res.getError().getMessage());
                    return new String[0];
                }

                // For full list of available annotations, see http://g.co/cloud/vision/docs
                texts = res.getFullTextAnnotation().getText().split("\n");
            }
        } catch (Exception exception) {
            Main.getInstance().getLogger().error("Error while trying to get the data", exception);
        }

        return texts;
    }
}
