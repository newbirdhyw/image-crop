# 소개
- 사용 버전: JDK 1.8
- 이미지를 가져온 후 병렬로 자른 후 이미지 저장
- 병렬 처리로 진행하기에 스레드를 정해준 개수 만큼 읽어서 자름
  - java -Djava.util.concurrent.ForkJoinPool.common.parallelism=10 -jar java-image-crop.jar
- 이미지 폴더만 지정하면 지정한 폴더에 log, output 폴더 생성이 자동으로 됨

# 성능
- 로컬
  - 개수: 300 / 완료 시간: 14초
- 실서버
  - 개수: 169,394 / 완료 시간: 6.4시간
