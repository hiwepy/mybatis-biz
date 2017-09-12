package org.mybatis.spring.utils;

import java.util.HashSet;
import java.util.Set;

import org.apache.ibatis.exception.MybatisException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

public class PackageScanUtils {

	public static ResourcePatternResolver resolver = (ResourcePatternResolver) new PathMatchingResourcePatternResolver();
	public static MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resolver);
	
	/**
	 * <p>
	 * 别名通配符设置
	 * </p>
	 * <p>
	 * <property name="typeAliasesPackage" value="org.apache.*.entity"/>
	 * </p>
	 * @param typeAliasesPackage 类别名包路径
	 * @return
	 */
	public static String[] scanTypeAliasesPackage(String typeAliasesPackage) {
		
		String pkg = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(typeAliasesPackage) + "/*.class";

		/*
		 * 将加载多个绝对匹配的所有Resource
		 * 将首先通过ClassLoader.getResource("META-INF")加载非模式路径部分，然后进行遍历模式匹配，排除重复包路径
		 */
		try {
			Set<String> set = new HashSet<String>();
			Resource[] resources = resolver.getResources(pkg);
			if (resources != null && resources.length > 0) {
				MetadataReader metadataReader = null;
				for (Resource resource : resources) {
					if (resource.isReadable()) {
						metadataReader = metadataReaderFactory.getMetadataReader(resource);
						set.add(Class.forName(metadataReader.getClassMetadata().getClassName()).getPackage().getName());
					}
				}
			}
			if (!set.isEmpty()) {
				return set.toArray(new String[] {});
			} else {
				throw new MybatisException("not find typeAliasesPackage:" + pkg);
			}
		} catch (Exception e) {
			throw new MybatisException("not find typeAliasesPackage:" + pkg, e);
		}
	}
	
}
