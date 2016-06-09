import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringPath;

/**
 * Created by vagrant on 6/9/16.
 */
public class predicate {

    public BooleanExpression getPredicate()
    {
        PathBuilder<playground> playPath = new PathBuilder<playground>(playground.class, "playground");
        StringPath path = playPath.getString("type");

        return path.containsIgnoreCase("swing");
    }

}
