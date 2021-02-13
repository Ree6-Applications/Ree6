package pw.aru.api.nekos4j.text;

import com.github.natanbc.reliqua.request.PendingRequest;
import pw.aru.api.nekos4j.Nekos4J;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import java.util.List;

@SuppressWarnings("unused")
public interface TextProvider {
    /**
     * Generates an text answer based on the specified text.
     *
     * @param text The text to generate an answer.
     * @return The generated answer.
     */
    @CheckReturnValue
    @Nonnull
    PendingRequest<String> generateAnswer(@Nonnull String text);

    /**
     * Returns the Nekos4J instance associated with this object.
     *
     * @return The Nekos4J instance associated with this object.
     */
    @CheckReturnValue
    @Nonnull
    Nekos4J getApi();

    /**
     * Generates an 8-ball answer based on the specified question.
     *
     * @return A list of endpoints.
     */
    @CheckReturnValue
    @Nonnull
    PendingRequest<List<String>> getEndpoints();

    /**
     * Gets a random 8-ball answer.
     *
     * @return A random answer.
     */
    @CheckReturnValue
    @Nonnull
    PendingRequest<Neko8Ball> getRandom8Ball();

    /**
     * Generates an 8-ball answer based on the specified question.
     *
     * @return The generated answer.
     */
    @CheckReturnValue
    @Nonnull
    PendingRequest<String> getRandomCat();

    /**
     * Generates an 8-ball answer based on the specified question.
     *
     * @return The generated answer.
     */
    @CheckReturnValue
    @Nonnull
    PendingRequest<String> getRandomFact();

    /**
     * Generates an 8-ball answer based on the specified question.
     *
     * @return The generated answer.
     */
    @CheckReturnValue
    @Nonnull
    PendingRequest<String> getRandomQuestion();

    /**
     * OwO-ifies an text based on the specified text.
     *
     * @param text The text to OwO-ify.
     * @return The generated answer.
     */
    @CheckReturnValue
    @Nonnull
    PendingRequest<String> owoifyText(@Nonnull String text);
}
