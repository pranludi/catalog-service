# 빌드
custom_build(
  ref = 'catalog-service',
  command = './gradlew bootBuildImage --imageName $EXPECTED_REF',
  deps = ['build.grade.kts', 'src']
)

# 배포
k8s_yaml(['k8s/deployment.yml', 'k8s/service.yml'])

# 관리
k8s_resource('catalog-service', port_forwards=['9001'])
