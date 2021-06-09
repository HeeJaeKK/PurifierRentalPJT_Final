# PurifierRentalPJT
21년 1차수 3조
# PurifierRentalProject (정수기렌탈 서비스)

3조 정수기 렌탈 신청 서비스 프로젝트 입니다.

# Table of contents

- [pirifierRentalProject (정수기 렌탈 신청 서비스)](#---)
  - [서비스 시나리오](#서비스-시나리오)
  - [체크포인트](#체크포인트)
  - [분석/설계](#분석설계)
  - [구현:](#구현-)
    - [DDD 의 적용](#ddd-의-적용)
    - [폴리글랏 퍼시스턴스](#폴리글랏-퍼시스턴스)
    - [폴리글랏 프로그래밍](#폴리글랏-프로그래밍)
    - [동기식 호출 과 Fallback 처리](#동기식-호출-과-Fallback-처리)
    - [비동기식 호출 과 Eventual Consistency](#비동기식-호출-과-Eventual-Consistency)
  - [운영](#운영)
    - [CI/CD 설정](#cicd설정)
    - [동기식 호출 / 서킷 브레이킹 / 장애격리](#동기식-호출-서킷-브레이킹-장애격리)
    - [오토스케일 아웃](#오토스케일-아웃)
    - [무정지 재배포](#무정지-재배포)
  - [신규 개발 조직의 추가](#신규-개발-조직의-추가)

# 서비스 시나리오

고객이 정수기 렌탈 서비스 가입신청을 하면 설치기사가 방문하여 설치를 하고, 가입 취소 시 취소 처리를 할 수 있도록 한다.

기능적 요구사항
1. 고객이 정수기 렌탈 서비스 가입신청을 한다.
1. 가입신청 접수가 되면, 자동으로 시스템이 가입요청 지역의 설치 기사에게 설치 요청이 된다.
1. 설치기사는 설치요청을 할당받는다.
1. 설치기사는 설치를 완료 후 설치 완료 처리를 한다.
1. 설치가 완료되면 정수기 렌탈 서비스 신청이 완료 처리가 된다.
1. 고객이 가입 신청을 취소할 수 있다.
1. 가입신청이 취소되면 설치 취소된다.(설치취소 처리는 Req/Res 테스트를 위해 임의로 동기처리)
1. 고객은 설치진행상태를 수시로 확인할 수 있다.

-추가-
1. 고객은 정수기 비용을 납부한다.
2. 받아야할 금액에서 이를 제하고 다시 납부를 완료한다.

비기능적 요구사항
1. 트랜잭션
    1. 가입취소 신청은 설치취소가 동시 이루어 지도록 한다
1. 장애격리
    1. 정수기 렌탈 가입신청과 취소는 고객서비스 담당자의 접수, 설치 처리와 관계없이 항상 처리 가능하다.

1. 성능
    1. 고객은 주문/설치 진행상태를 수시로 확인한다.(CQRS)



# 체크포인트

- 분석 설계


  - 이벤트스토밍: 
    - 스티커 색상별 객체의 의미를 제대로 이해하여 헥사고날 아키텍처와의 연계 설계에 적절히 반영하고 있는가?
    - 각 도메인 이벤트가 의미있는 수준으로 정의되었는가?
    - 어그리게잇: Command와 Event 들을 ACID 트랜잭션 단위의 Aggregate 로 제대로 묶었는가?
    - 기능적 요구사항과 비기능적 요구사항을 누락 없이 반영하였는가?    

  - 서브 도메인, 바운디드 컨텍스트 분리
    - 팀별 KPI 와 관심사, 상이한 배포주기 등에 따른  Sub-domain 이나 Bounded Context 를 적절히 분리하였고 그 분리 기준의 합리성이 충분히 설명되는가?
      - 적어도 3개 이상 서비스 분리
    - 폴리글랏 설계: 각 마이크로 서비스들의 구현 목표와 기능 특성에 따른 각자의 기술 Stack 과 저장소 구조를 다양하게 채택하여 설계하였는가?
    - 서비스 시나리오 중 ACID 트랜잭션이 크리티컬한 Use 케이스에 대하여 무리하게 서비스가 과다하게 조밀히 분리되지 않았는가?
  - 컨텍스트 매핑 / 이벤트 드리븐 아키텍처 
    - 업무 중요성과  도메인간 서열을 구분할 수 있는가? (Core, Supporting, General Domain)
    - Request-Response 방식과 이벤트 드리븐 방식을 구분하여 설계할 수 있는가?
    - 장애격리: 서포팅 서비스를 제거 하여도 기존 서비스에 영향이 없도록 설계하였는가?
    - 신규 서비스를 추가 하였을때 기존 서비스의 데이터베이스에 영향이 없도록 설계(열려있는 아키택처)할 수 있는가?
    - 이벤트와 폴리시를 연결하기 위한 Correlation-key 연결을 제대로 설계하였는가?

  - 헥사고날 아키텍처
    - 설계 결과에 따른 헥사고날 아키텍처 다이어그램을 제대로 그렸는가?
    
- 구현
  - [DDD] 분석단계에서의 스티커별 색상과 헥사고날 아키텍처에 따라 구현체가 매핑되게 개발되었는가?
    - Entity Pattern 과 Repository Pattern 을 적용하여 JPA 를 통하여 데이터 접근 어댑터를 개발하였는가
    - [헥사고날 아키텍처] REST Inbound adaptor 이외에 gRPC 등의 Inbound Adaptor 를 추가함에 있어서 도메인 모델의 손상을 주지 않고 새로운 프로토콜에 기존 구현체를 적응시킬 수 있는가?
    - 분석단계에서의 유비쿼터스 랭귀지 (업무현장에서 쓰는 용어) 를 사용하여 소스코드가 서술되었는가?
  - Request-Response 방식의 서비스 중심 아키텍처 구현
    - 마이크로 서비스간 Request-Response 호출에 있어 대상 서비스를 어떠한 방식으로 찾아서 호출 하였는가? (Service Discovery, REST, FeignClient)
    - 서킷브레이커를 통하여  장애를 격리시킬 수 있는가?
  - 이벤트 드리븐 아키텍처의 구현
    - 카프카를 이용하여 PubSub 으로 하나 이상의 서비스가 연동되었는가?
    - Correlation-key:  각 이벤트 건 (메시지)가 어떠한 폴리시를 처리할때 어떤 건에 연결된 처리건인지를 구별하기 위한 Correlation-key 연결을 제대로 구현 하였는가?
    - Message Consumer 마이크로서비스가 장애상황에서 수신받지 못했던 기존 이벤트들을 다시 수신받아 처리하는가?
    - Scaling-out: Message Consumer 마이크로서비스의 Replica 를 추가했을때 중복없이 이벤트를 수신할 수 있는가
    - CQRS: Materialized View 를 구현하여, 타 마이크로서비스의 데이터 원본에 접근없이(Composite 서비스나 조인SQL 등 없이) 도 내 서비스의 화면 구성과 잦은 조회가 가능한가?

  - 폴리글랏 플로그래밍
    - 각 마이크로 서비스들이 하나이상의 각자의 기술 Stack 으로 구성되었는가?
    - 각 마이크로 서비스들이 각자의 저장소 구조를 자율적으로 채택하고 각자의 저장소 유형 (RDB, NoSQL, File System 등)을 선택하여 구현하였는가?
  - API 게이트웨이
    - API GW를 통하여 마이크로 서비스들의 집입점을 통일할 수 있는가?
    - 게이트웨이와 인증서버(OAuth), JWT 토큰 인증을 통하여 마이크로서비스들을 보호할 수 있는가?
- 운영
  - SLA 준수
    - 셀프힐링: Liveness Probe 를 통하여 어떠한 서비스의 health 상태가 지속적으로 저하됨에 따라 어떠한 임계치에서 pod 가 재생되는 것을 증명할 수 있는가?
    - 서킷브레이커, 레이트리밋 등을 통한 장애격리와 성능효율을 높힐 수 있는가?
    - 오토스케일러 (HPA) 를 설정하여 확장적 운영이 가능한가?
    - 모니터링, 앨럿팅: 
  - 무정지 운영 CI/CD (10)
    - Readiness Probe 의 설정과 Rolling update을 통하여 신규 버전이 완전히 서비스를 받을 수 있는 상태일때 신규버전의 서비스로 전환됨을 siege 등으로 증명 
    - Contract Test :  자동화된 경계 테스트를 통하여 구현 오류나 API 계약위반를 미리 차단 가능한가?
    - 


# 분석/설계


## AS-IS 조직 (Horizontally-Aligned)
![as_is](https://user-images.githubusercontent.com/81946287/118763701-4323ab80-b8b3-11eb-9a23-15da2ea74528.png)

## TO-BE 조직 (Vertically-Aligned)
![to_be](https://user-images.githubusercontent.com/81946287/118763808-6f3f2c80-b8b3-11eb-807c-75ddb2daaddd.png)


## Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과:  
  - http://www.msaez.io/#/storming/IkrhsmX1DtOtbLQM9ri9QJ9uoRm2/every/ee3188d76ffc9344628b8a9183bcc9b1

### 이벤트 도출
![Event](https://user-images.githubusercontent.com/81946287/118763908-9d247100-b8b3-11eb-992f-f930e774b284.png)


### 폴리시,어그리게잇 부착
![Policy](https://user-images.githubusercontent.com/81946287/118763987-bb8a6c80-b8b3-11eb-9fe4-81c8f0380262.png)

    -가입신청, 서비스관리센터, 설치 부분을 정의함

### 액터, 커맨드 부착
![Command](https://user-images.githubusercontent.com/81946287/118764046-d1982d00-b8b3-11eb-9f90-87bf95f1660e.png)


### 바운디드 컨텍스트로 묶기
![BoundedContext](https://user-images.githubusercontent.com/81946287/118764169-099f7000-b8b4-11eb-9b65-acc48be3d56c.png)

    - 도메인 서열 분리 : 가입신청 -> 서비스관리센터 -> 설치 순으로 정의
       

### View모델 추가 및 폴리시의 이동과 컨텍스트 매핑 (파란색점선은 Pub/Sub, 빨간색실선은 Req/Resp)
![AddView](https://user-images.githubusercontent.com/81946287/118764287-38b5e180-b8b4-11eb-997c-92032adf193d.png)


### 완성된 1차 모형
![firstDesign](https://user-images.githubusercontent.com/81946287/118764341-4d927500-b8b4-11eb-8345-71254fe30f70.png)

### 수정된 2차 모형
![2ndDesign](https://user-images.githubusercontent.com/81946287/118765229-bc240280-b8b5-11eb-8bf4-2015470e7987.png)

    - 시나리오 내용을 매끄럽게 반영하기 위해 '서비스관리센터'를 '배정' 으로 변경
    - 가입신청과 동시에 자동 배정되는 요구사항에 따라 Manager 액터가 불필요하여 제거
    - 고객이 실시간 상태 확인을 위한 View 모델 배치

### 개인과제 3차 모형
![개인과제](https://user-images.githubusercontent.com/50816895/121023703-7edcd180-c7de-11eb-8f36-758a9aa80802.JPG)

### 2차 완성본에 대한 기능적/비기능적 요구사항을 커버하는지 검증
#### 시나리오 Coverage Check (1)
![1stReview](https://user-images.githubusercontent.com/81946287/118766395-546eb700-b8b7-11eb-8330-a26f30c69072.png)

#### 시나리오 Coverage Check (2)
![2ndReview](https://user-images.githubusercontent.com/81946287/118766439-62243c80-b8b7-11eb-825d-9fcc9635607c.png)

#### 비기능 요구사항 coverage
![3rdReview](https://user-images.githubusercontent.com/81946287/118766471-6cded180-b8b7-11eb-9c00-dcaec093281c.png)

#### 개인과제 Coverage Check
![image](https://user-images.githubusercontent.com/50816895/121023956-ba779b80-c7de-11eb-91ab-351e7cfe492e.png)

## 헥사고날 아키텍처 다이어그램 도출
![image](https://user-images.githubusercontent.com/50816895/121024837-9072a900-c7df-11eb-96af-8cb4ce0c1eca.png)

## 신규 서비스 추가 시 기존 서비스에 영향이 없도록 열린 아키택처 설계

- 신규 개발 조직 추가 시, 기존의 마이크로 서비스에 수정이 발생하지 않도록 Inbund 요청을 REST 가 아닌 Event를 Subscribe 하는 방식으로 구현하였다.
- 기존 마이크로 서비스에 대하여 아키텍처, 데이터베이스 구조와 관계 없이 추가할 수 있다.

![hexagonal2](https://user-images.githubusercontent.com/81946287/118780023-97845680-b8c6-11eb-89d3-01fabd32fbfa.png)



# 구현:
분석/설계 단계에서 도출된 헥사고날 아키텍처에 따라, 각 BC별로 대변되는 마이크로 서비스들을 스프링부트로 구현하였다. 구현한 각 서비스를 로컬에서 실행하는 방법은 아래와 같다 (각자의 포트넘버는 8081 ~ 8083 이다)

```
- Local
	cd Order
	mvn spring-boot:run

	cd Assignment
	mvn spring-boot:run

	cd Installation
	mvn spring-boot:run
	
	cd Payment
	mvn spring-boot:run


- EKS : CI/CD 통해 빌드/배포 ("운영 > CI-CD 설정" 부분 참조)
```

## DDD 의 적용

- 각 서비스내에 도출된 핵심 Aggregate Root 객체를 Entity 로 선언하였다: Order, Assignment, Installation
- Assignment(배정) 마이크로서비스 예시

```
	package purifierrentalpjt;

	import javax.persistence.*;
	import org.springframework.beans.BeanUtils;
	
	import lombok.Getter;
	import lombok.Setter;
	
	import java.util.List;
	import java.util.Date;

	@Entity
	@Getter
	@Setter
	@Table(name="Assignment_table")
	public class Assignment {
		
		@Id
    		@GeneratedValue(strategy=GenerationType.AUTO)
    		private Long id;
    		private Long orderId;
    		private String installationAddress;
    		private Long engineerId;
    		private String engineerName;
    		private String status;

    		@PostPersist
    		public void onPostPersist(){
        
        		System.out.println(this.getStatus() + "POST TEST");
        
        		if(this.getStatus().equals("orderRequest")) {

            		  EngineerAssigned engineerAssigned = new EngineerAssigned();

            		  engineerAssigned.setId(this.getId()); 
            		  engineerAssigned.setOrderId(this.getId()); 
            		  engineerAssigned.setInstallationAddress(this.getInstallationAddress()); 
            		  engineerAssigned.setEngineerId(this.getEngineerId()); 
            		  engineerAssigned.setEngineerName(this.getEngineerName()); 
            
            		  BeanUtils.copyProperties(this, engineerAssigned);
            		  engineerAssigned.publishAfterCommit();

        		} else if (this.getStatus().equals("installationComplete")) {

            		  JoinCompleted joinCompleted = new JoinCompleted();

            		  joinCompleted.setId(this.getId()); 
            		  joinCompleted.setOrderId(this.orderId); 
            		  joinCompleted.setStatus(this.getStatus());
```

적용 후 REST API의 테스트
1) 정수기 렌탈 서비스 신청 & 설치완료 처리

- (a) http -f POST localhost:8081/order/joinOrder productId=4 productName=PURI6 installationAddress="addr#6" customerId=506
- (b) http -f PATCH http://localhost:8083/installations orderId=5 
![image](https://user-images.githubusercontent.com/76420081/118930671-00c8a000-b981-11eb-9af5-3619d4ceaedd.png)

2) 카프카 메시지 확인

- (a) 서비스 신청 후 : JoinOrdered -> EngineerAssigned -> InstallationAccepted
- (b) 설치완료 처리 후 : InstallationCompleted
![image](https://user-images.githubusercontent.com/76420081/118930569-df67b400-b980-11eb-8ad2-66e33a3a5993.png)


## 폴리글랏 퍼시스턴스
- order, Assignment, installation 서비스 모두 H2 메모리DB를 적용하였다.  
다양한 데이터소스 유형 (RDB or NoSQL) 적용 시 데이터 객체에 @Entity 가 아닌 @Document로 마킹 후, 기존의 Entity Pattern / Repository Pattern 적용과 데이터베이스 제품의 설정 (application.yml) 만으로 가능하다.

```
--application.yml // mariaDB 추가 예시
spring:
  profiles: real-db
  datasource:
        url: jdbc:mariadb://rds주소:포트명(기본은 3306)/database명
        username: db계정
        password: db계정 비밀번호
        driver-class-name: org.mariadb.jdbc.Driver
```

## 동기식 호출 과 Fallback 처리

- 분석 단계에서의 조건 중 하나로 배정(Assignment) 서비스에서 인터넷 가입신청 취소를 요청 받으면, 
설치(installation) 서비스 취소 처리하는 부분을 동기식 호출하는 트랜잭션으로 처리하기로 하였다. 
- 호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어 있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다.

설치 서비스를 호출하기 위하여 Stub과 (FeignClient) 를 이용하여 Service 대행 인터페이스 (Proxy) 를 구현
```
# (Assignment) InstallationService.java

	package purifierrentalpjt.external;
	
	import org.springframework.cloud.openfeign.FeignClient;
	import org.springframework.web.bind.annotation.RequestBody;
	import org.springframework.web.bind.annotation.RequestMapping;
	import org.springframework.web.bind.annotation.RequestMethod;

	/**
 	 * 설치subsystem 동기호출
 	 * @author Administrator
 	 * 아래 주소는 Gateway주소임
 	*/


	@FeignClient(name="Installation", url="http://installation:8080")
	//@FeignClient(name="Installation", url="http://localhost:8083")
	public interface InstallationService {

		@RequestMapping(method= RequestMethod.POST, path="/installations")
    		public void cancelInstallation(@RequestBody Installation installation);

	}
```

정수기 렌탈 서비스 가입 취소 요청(cancelRequest)을 받은 후, 처리하는 부분
```
# (Installation) InstallationController.java

	package purifierrentalpjt;

	@RestController
	public class InstallationController {

    	  @Autowired
    	  InstallationRepository installationRepository;

    	  /**
     	   * 설치취소
     	   * @param installation
           */
	  @RequestMapping(method=RequestMethod.POST, path="/installations")
    	  public void installationCancellation(@RequestBody Installation installation) {
    	
    		System.out.println( "### 동기호출 -설치취소=" +ToStringBuilder.reflectionToString(installation) );

    		Optional<Installation> opt = installationRepository.findByOrderId(installation.getOrderId());
    		if( opt.isPresent()) {
    			Installation installationCancel =opt.get();
    			installationCancel.setStatus("installationCanceled");
    			installationRepository.save(installationCancel);
    		} else {
    			System.out.println("### 설치취소 - 못찾음");
    		}
    	}
```

## 비동기식 호출 / 시간적 디커플링 / 장애격리 / 최종 (Eventual) 일관성 테스트

가입 신청(order)이 이루어진 후에 배정(Assignment) 서비스로 이를 알려주는 행위는 비동기식으로 처리하여, 배정(Assignment) 서비스의 처리를 위하여 가입신청(order)이 블로킹 되지 않도록 처리한다.
 
- 이를 위하여 가입 신청에 기록을 남긴 후에 곧바로 가입 신청이 되었다는 도메인 이벤트를 카프카로 송출한다.(Publish)
```
# (order) Order.java

    @PostPersist
    public void onPostPersist(){

        JoinOrdered joinOrdered = new JoinOrdered();
        BeanUtils.copyProperties(this, joinOrdered);
        joinOrdered.publishAfterCommit();
    }
```
- 배정 서비스에서는 가입신청 이벤트에 대해서 이를 수신하여 자신의 정책을 처리하도록 PolicyHandler 를 구현한다.
```
# (Assignment) PolicyHandler.java

@Service
public class PolicyHandler{
    @Autowired AssignmentRepository assignmentRepository;

    @StreamListener(KafkaProcessor.INPUT)
    public void wheneverJoinOrdered_OrderRequest(@Payload JoinOrdered joinOrdered){

        if(!joinOrdered.validate()) return;

        System.out.println("\n\n##### listener OrderRequest : " + joinOrdered.toJson() + "\n\n");

        Assignment assignment = new Assignment();

        assignment.setId(joinOrdered.getId());
        assignment.setInstallationAddress(joinOrdered.getInstallationAddress());
        assignment.setStatus("orderRequest");
        assignment.setEngineerName("Enginner" + joinOrdered.getId());
        assignment.setEngineerId(joinOrdered.getId());
        assignment.setOrderId(joinOrdered.getId());

        assignmentRepository.save(assignment);
        }
    }
}
```
가입신청은 배정 서비스와 완전히 분리되어 있으며, 이벤트 수신에 따라 처리되기 때문에, 배정 서비스가 유지보수로 인해 잠시 내려간 상태라도 가입신청을 받는데 문제가 없다.


## CQRS

가입신청 상태 조회를 위한 서비스를 CQRS 패턴으로 구현하였다.
- Order, Assignment, Installation, Payment 개별 aggregate 통합 조회로 인한 성능 저하를 막을 수 있다.
- 모든 정보는 비동기 방식으로 발행된 이벤트를 수신하여 처리된다.
- 설계 : MSAEz 설계의 view 매핑 설정 참조

- 주문생성

![image](https://user-images.githubusercontent.com/76420081/119001165-b23df480-b9c6-11eb-9d62-bed7406f0709.png)

- 카프카 메시지

![image](https://user-images.githubusercontent.com/76420081/119001370-df8aa280-b9c6-11eb-867f-fbd78ab89031.png)

- 주문취소

![image](https://user-images.githubusercontent.com/76420081/119001667-25476b00-b9c7-11eb-8609-c6a7e9a02dfe.png)

- 카프카 메시지

![image](https://user-images.githubusercontent.com/76420081/119001720-32645a00-b9c7-11eb-81aa-58191e7bef1d.png)

- 뷰테이블 수신처리

![image](https://user-images.githubusercontent.com/76420081/119002598-fa114b80-b9c7-11eb-9aac-ed6ac136be4c.png)


## API Gateway

API Gateway를 통하여, 마이크로 서비스들의 진입점을 통일한다.

```
# application.yml 파일에 라우팅 경로 설정

spring:
  profiles: docker
  cloud:
    gateway:
      routes:
        - id: order
          uri: http://order:8080
          predicates:
            - Path=/order/**,/orders/**,/orderStatuses/**
        - id: assignment
          uri: http://assignment:8080
          predicates:
            - Path=/assignments/** 
        - id: installation
          uri: http://installation:8080
          predicates:
            - Path=/installations/** 
        - id: payment
          uri: http://payment:8080
          predicates:
            - Path=/payments/** 
      globalcors:
        corsConfigurations:
          '[/**]':
            allowedOrigins:
              - "*"
            allowedMethods:
              - "*"
            allowedHeaders:
              - "*"
            allowCredentials: true

server:
  port: 8080
```

- EKS에 배포 시, MSA는 Service type을 ClusterIP(default)로 설정하여, 클러스터 내부에서만 호출 가능하도록 한다.
- API Gateway는 Service type을 LoadBalancer로 설정하여 외부 호출에 대한 라우팅을 처리한다.



# 운영

## CI/CD 설정
### 빌드/배포
각 프로젝트 jar를 Dockerfile을 통해 Docker Image 만들어 ECR저장소에 올린다.   
EKS 클러스터에 접속한 뒤, 각 서비스의 deployment.yaml, service.yaml을 kuectl명령어로 서비스를 배포한다.   
  - 코드 형상관리 : https://github.com/HeeJaeKK/PurifierRentalPJT_Final 하위 repository에 각각 구성   
  - 운영 플랫폼 : AWS의 EKS(Elastic Kubernetes Service)   
  - Docker Image 저장소 : AWS의 ECR(Elastic Container Registry)
##### 배포 명령어
```
$ kubectl apply -f deployment.yml
$ kubectl apply -f service.yaml
```

##### 배포 결과
![운영 Deploy](https://user-images.githubusercontent.com/50816895/121293664-f453b980-c926-11eb-90d6-b0feca07168a.JPG)

## 동기식 호출 / 서킷 브레이킹 / 장애격리

* 서킷 브레이킹 프레임워크의 선택
  - Spring FeignClient + Hystrix 옵션을 사용하여 구현할 경우, 도메인 로직과 부가 기능 로직이 서비스에 같이 구현된다.
  - istio를 사용해서 서킷 브레이킹 적용이 가능하다.

- istio 설치

![istio](https://user-images.githubusercontent.com/50816895/121296094-09cae280-c92b-11eb-867e-9a43e4c37448.JPG)
                                                                                                              
- istio 에서 서킷브레이커 설정(DestinationRule)
```
cat <<EOF | kubectl apply -f -
apiVersion: networking.istio.io/v1alpha3
kind: DestinationRule
metadata:
  name: installation
spec:
  host: installation
  trafficPolicy:
    connectionPool:
      tcp:
        maxConnections: 1           # 목적지로 가는 HTTP, TCP connection 최대 값. (Default 1024)
      http:
        http1MaxPendingRequests: 1  # 연결을 기다리는 request 수를 1개로 제한 (Default 
        maxRequestsPerConnection: 1 # keep alive 기능 disable
        maxRetries: 3               # 기다리는 동안 최대 재시도 수(Default 1024)
    outlierDetection:
      consecutiveErrors: 5          # 5xx 에러가 5번 발생하면
      interval: 1s                  # 1초마다 스캔 하여
      baseEjectionTime: 30s         # 30 초 동안 circuit breaking 처리   
      maxEjectionPercent: 100       # 100% 로 차단
EOF

```

* 부하테스터 siege 툴을 통한 서킷 브레이커 동작을 확인한다.
- 동시사용자 100명
- 60초 동안 실시
- 결과 화면
![image](https://user-images.githubusercontent.com/50816895/121295944-d38d6300-c92a-11eb-8531-ed149b1d7147.png)

### Liveness
pod의 container가 정상적으로 기동되는지 확인하여, 비정상 상태인 경우 pod를 재기동하도록 한다.   

아래의 값으로 liveness를 설정한다.
- 재기동 제어값 : /tmp/healthy 파일의 존재를 확인
- 기동 대기 시간 : 3초
- 재기동 횟수 : 5번까지 재시도

이때, 재기동 제어값인 /tmp/healthy파일을 강제로 지워 liveness가 pod를 비정상 상태라고 판단하도록 하였다.    
5번 재시도 후에도 파드가 뜨지 않았을 경우 CrashLoopBackOff 상태가 됨을 확인하였다.   
##### order에 Liveness 적용한 내용
```yaml
apiVersion: apps/v1
kind: Deployment
...
    spec:
      containers:
        - name: order
          image: 879772956301.dkr.ecr.ap-northeast-2.amazonaws.com/user04-puri-order:v3
          args:
          - /bin/sh
          - -c
          - touch /tmp/healthy; sleep 10; rm -rf /tmp/healthy; sleep 600;
...
          livenessProbe:
            httpGet:
              path: '/actuator/health'
              port: 8080
            initialDelaySeconds: 3
            timeoutSeconds: 2
            periodSeconds: 5
            failureThreshold: 5
```
- CrashLoopBackOff 상태 확인
![image](https://user-images.githubusercontent.com/50816895/121301601-65996980-c933-11eb-852b-e395f31b524e.png)

### 오토스케일 아웃

- 가입신청 서비스에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 1프로를 넘어서면 replica 를 10개까지 늘려준다.
```
kubectl autoscale deploy order --min=1 --max=10 --cpu-percent=1
```
- deployment.yaml
```apiVersion: apps/v1
kind: Deployment
...
    spec:
      containers:
        - name: order
          resources:
            limits: 
              cpu: 500m
            requests:
              cpu: 200m
          image: 879772956301.dkr.ecr.ap-northeast-2.amazonaws.com/user04-puri-order:v4
```
- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어준다.

```
kubectl get deploy order -w

kubectl get hpa order -w
```

- 사용자 50명으로 워크로드를 3분 동안 걸어준다.
```
siege -c50 -t180S  -v 'http://a5f9e103b958a4b9d87150880b498020-128284363.ap-northeast-2.elb.amazonaws.com:8080/order/joinOrder POST productId=4&productName=PURI4&installationAddress=Dongtan&customerId=504'

```

- 오토스케일 발생하지 않음(siege 실행 결과 오류 없이 수행됨 : Availability 100%)
- autoscale 대상이 unknown이며, 확인결과 메트릭스 서버 설치필요한 것으로 보임

![image](https://user-images.githubusercontent.com/76420081/119087445-1ce04600-ba42-11eb-92c8-2f0e2d772562.png)

- 메트릭스 서버 설치
```
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
kubectl get deployment metrics-server -n kube-system
```
![image](https://user-images.githubusercontent.com/50816895/121300111-5b766b80-c931-11eb-8b4a-19a56244901c.png)
![image](https://user-images.githubusercontent.com/50816895/121300180-7648e000-c931-11eb-8d7b-ec23c13621ac.png)



## 무정지 재배포

* 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscaler 이나 서킷브레이커 설정을 제거함

- seige 로 배포작업 직전에 워크로드를 모니터링 한다.
```
siege -c50 -t180S  -v 'http://a39e59e8f1e324d23b5546d96364dc45-974312121.ap-southeast-2.elb.amazonaws.com:8080/order/joinOrder POST productId=4&productName=PURI4&installationAddress=Dongtan&customerId=504'
```

- readinessProbe, livenessProbe 설정되지 않은 상태로 buildspec.yml을 수정한다.
- Github에 buildspec.yml 수정 발생으로 CodeBuild 자동 빌드/배포 수행된다.
- siege 수행 결과 : 

- readinessProbe, livenessProbe 설정하고 buildspec.yml을 수정한다.
- Github에 buildspec.yml 수정 발생으로 CodeBuild 자동 빌드/배포 수행된다.
- siege 수행 결과 : 


## ConfigMap 적용

- 설정의 외부 주입을 통한 유연성을 제공하기 위해 ConfigMap을 적용한다.
- orderstatus 에서 사용하는 mySQL(AWS RDS 활용) 접속 정보를 ConfigMap을 통해 주입 받는다.

```
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: ConfigMap
metadata:
  name: order
data:
  urlstatus: "jdbc:mysql://order.cgzkudckye4b.ap-southeast-2:3306/orderstatus?serverTimezone=UTC&useUnicode=true&characterEncoding=utf8"
EOF
```

## Secret 적용

- username, password와 같은 민감한 정보는 ConfigMap이 아닌 Secret을 적용한다.
- etcd에 암호화 되어 저장되어, ConfigMap 보다 안전하다.
- value는 base64 인코딩 된 값으로 지정한다. (echo root | base64)

```
cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Secret
metadata:
  name: order
type: Opaque
data:
  username: xxxxx <- 보안 상, 임의의 값으로 표시함 
  password: xxxxx <- 보안 상, 임의의 값으로 표시함
EOF
```
