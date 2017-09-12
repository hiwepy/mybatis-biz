package org.apache.ibatis.jdbc;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.MybatisContext;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.apache.ibatis.utils.ResultUtils;
import org.apache.ibatis.utils.StringUtils;

public class MybatisSqlRunner {

	public static final int NO_GENERATED_KEY = Integer.MIN_VALUE + 1001;

	private Connection connection;
	private TypeHandlerRegistry typeHandlerRegistry;
	private boolean useGeneratedKeySupport;
	
	// 默认FACTORY
	public static SqlSessionFactory FACTORY;

	public static final String INSERT = "org.apache.ibatis.jdbc.MybatisSqlRunner.Insert";
	public static final String DELETE = "org.apache.ibatis.jdbc.MybatisSqlRunner.Delete";
	public static final String UPDATE = "org.apache.ibatis.jdbc.MybatisSqlRunner.Update";
	public static final String SELECT = "org.apache.ibatis.jdbc.MybatisSqlRunner.Select";
	public static final String COUNT = "org.apache.ibatis.jdbc.MybatisSqlRunner.Count";
	public static final String SQLScript = "${sql}";
	public static final String SQL = "sql";

	// 单例Query
	public static final MybatisSqlRunner DEFAULT = new MybatisSqlRunner();

	private SqlSessionFactory sqlSessionFactory;

	private Class<?> clazz;

	public MybatisSqlRunner() {
		this.sqlSessionFactory = FACTORY;
	}

	public MybatisSqlRunner(Class<?> clazz) {
		this.clazz = clazz;
	}

	public MybatisSqlRunner(Connection connection) {
		this.connection = connection;
		this.typeHandlerRegistry = new TypeHandlerRegistry();
	}
	

	public boolean insert(String sql, Object... args) {
		return ResultUtils.retBool(sqlSession().insert(INSERT, sqlMap(sql, args)));
	}

	public boolean delete(String sql, Object... args) {
		return ResultUtils.retBool(sqlSession().delete(DELETE, sqlMap(sql, args)));
	}

	/**
	 * 获取sqlMap参数
	 * 
	 * @param sql
	 * @param args
	 * @return
	 */
	private Map<String, String> sqlMap(String sql, Object... args) {
		Map<String, String> sqlMap = new HashMap<String, String>();
		sqlMap.put(SQL, StringUtils.sqlArgsFill(sql, args));
		return sqlMap;
	}

	public boolean update(String sql, Object... args) {
		return ResultUtils.retBool(sqlSession().update(UPDATE, sqlMap(sql, args)));
	}

	public List<Map<String, Object>> selectList(String sql, Object... args) {
		return sqlSession().selectList(SELECT, sqlMap(sql, args));
	}

	public int selectCount(String sql, Object... args) {
		return ResultUtils.retCount(sqlSession().<Integer> selectOne(COUNT, sqlMap(sql, args)));
	}

	public Map<String, Object> selectOne(String sql, Object... args) {
		return ResultUtils.getObject(selectList(sql, args));
	}

	/*@SuppressWarnings({ "unchecked", "rawtypes" })
	public Page<Map<String, Object>> selectPage(Page page, String sql, Object... args) {
		if (null == page) {
			return null;
		}
		page.setRecords(sqlSession().selectList(SELECT, sqlMap(sql, args), page));
		return page;
	}*/

	/**
	 * 获取默认的SqlQuery(适用于单库)
	 * 
	 * @return
	 */
	public static MybatisSqlRunner db() {
		// 初始化的静态变量 还是有前后加载的问题 该判断只会执行一次
		if (DEFAULT.sqlSessionFactory == null) {
			DEFAULT.sqlSessionFactory = FACTORY;
		}
		return DEFAULT;
	}

	/**
	 * 根据当前class对象获取SqlQuery(适用于多库)
	 * 
	 * @param clazz
	 * @return
	 */
	public static MybatisSqlRunner db(Class<?> clazz) {
		return new MybatisSqlRunner(clazz);
	}

	/**
	 * <p>
	 * 获取Session 默认自动提交
	 * <p/>
	 */
	private SqlSession sqlSession() {
		return (clazz != null) ? ResultUtils.sqlSession(clazz) : MybatisContext.getSqlSession(FACTORY.getConfiguration());
	}

}
