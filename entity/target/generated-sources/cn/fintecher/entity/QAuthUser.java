package cn.fintecher.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QAuthUser is a Querydsl query type for AuthUser
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QAuthUser extends EntityPathBase<AuthUser> {

    private static final long serialVersionUID = -1224187077L;

    public static final QAuthUser authUser = new QAuthUser("authUser");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final DateTimePath<java.util.Date> date_joined = createDateTime("date_joined", java.util.Date.class);

    public final StringPath email = createString("email");

    public final StringPath first_name = createString("first_name");

    //inherited
    public final StringPath id = _super.id;

    public final NumberPath<Integer> is_staff = createNumber("is_staff", Integer.class);

    public final NumberPath<Integer> is_superuser = createNumber("is_superuser", Integer.class);

    public final DateTimePath<java.util.Date> last_login = createDateTime("last_login", java.util.Date.class);

    public final StringPath last_name = createString("last_name");

    public final StringPath password = createString("password");

    public final StringPath username = createString("username");

    public QAuthUser(String variable) {
        super(AuthUser.class, forVariable(variable));
    }

    public QAuthUser(Path<? extends AuthUser> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAuthUser(PathMetadata metadata) {
        super(AuthUser.class, metadata);
    }

}

