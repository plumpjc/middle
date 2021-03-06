package kr.or.ddit.shop.view.join;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import kr.or.ddit.shop.service.join.IJoinService;
import kr.or.ddit.shop.vo.member.MemAllVO;
import kr.or.ddit.shop.vo.member.MemDetailVO;
import kr.or.ddit.shop.vo.zipcode.ZipcodeVO;

public class JoinController implements Initializable {

	@FXML Button btnCaptchachk;
	@FXML Button btnSubmit;
	@FXML Button btnCancel;
	@FXML Button btnRefresh;
	@FXML Button btnIdchk;
	@FXML Button btnNickchk;
	@FXML Button btnFindzip;
	@FXML TextField inputCaptcha;
	@FXML TextField inputEmail;
	@FXML TextField inputName;
	@FXML TextField inputBirth;
	@FXML TextField inputHP;
	@FXML TextField inputNick;
	@FXML TextField inputZip;
	@FXML TextField inputAddr;
	@FXML PasswordField inputPW;
	@FXML PasswordField inputPW2;

	private Registry reg;
	private IJoinService ijs;

	List<MemDetailVO> mDlist = new ArrayList<MemDetailVO>();
	private MemDetailVO mvo = new MemDetailVO();
	int chk_cnt = 0;
	int pattern_cnt = 0;
	int no_cnt = 0;
	boolean result = false;
	private boolean captchaFlag = false;
	private String captchaKey = "";
	File f = null; // captchaimg파일
	String captchaImg = ""; // captcha img 파일이름.
	
	
	String zip = null;
	String gungu = null;
	String sido = null;
	String dong = null;

	AES256 aes;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		captchaKey = captchaKey();
		captchaImage(captchaKey);
		
		try {
			reg = LocateRegistry.getRegistry("localhost", 9809);
			ijs = (IJoinService) reg.lookup("userjoin");
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e1) {
			e1.printStackTrace();
		}
		
		btnIdchk.setOnAction(e -> {
			String mem_mail = inputEmail.getText();
			Pattern p1 = Pattern.compile("\\w*@[a-z]*\\.[a-z]{3}");
			Matcher m1 = p1.matcher(mem_mail);
			result = m1.matches();
			
			if (inputEmail.getText().equals("") || inputEmail.getText() == null) {
				errMsg("중복체크", "아이디를 입력해주십시오.");
				return;
			} else if(result == false) {
				errMsg("에러!", "형식에 맞지 않습니다.");
				return;
			} 
			ArrayList<MemDetailVO> mDList = new ArrayList<>(); 

			MemDetailVO mvo = new MemDetailVO();
			mvo.setMem_mail(mem_mail);

			try {
				mDList = (ArrayList<MemDetailVO>) ijs.selectByMail(mem_mail);
			} catch (RemoteException e2) {
				e2.printStackTrace();
			}

			System.out.println(mDList.size());
			if (mDList.size() == 0) {
				infoMsg("중복체크", "사용하실 수 있는 아이디 입니다.");
				chk_cnt++;
				pattern_cnt++;
				System.out.println("아이디 중복체크" + chk_cnt);
			} else {
				errMsg("중복체크", "이미 사용하고 있는 아이디 입니다.");
				return;
			}
		});

		
		btnNickchk.setOnAction(e -> {
			String mem_nick = inputNick.getText();
			Pattern p999 = Pattern.compile("^[가-힣]*$");
			Matcher m999 = p999.matcher(mem_nick);
			result = m999.matches();
			
			if (mem_nick.equals("") || mem_nick == null) {
				errMsg("에러!", "닉네임을 입력하지 않으셨습니다!");
				no_cnt++;
				return;
			}else if(result == false) {
				errMsg("에러!", "형식에 맞지 않는 닉네임 입니다!");
				return;
			}
			
			ArrayList<MemDetailVO> mDList = new ArrayList<>();

			MemDetailVO mvo = new MemDetailVO();
			mvo.setMem_nick(mem_nick);

			try {
				mDList = (ArrayList<MemDetailVO>) ijs.selectByNick(mem_nick);
			} catch (RemoteException e2) {
				e2.printStackTrace();
			}

			System.out.println(mDList.size());
			if (mDList.size() == 0) {
				infoMsg("중복체크", "사용하실 수 있는 닉네임 입니다.");
				chk_cnt++;
				pattern_cnt++;
				System.out.println("닉네임 중복체크" + chk_cnt);
			} else {
				errMsg("중복체크", "이미 사용하고 있는 닉네임 입니다.");
				return;
			}

		});

		btnFindzip.setOnAction(e -> {
			Parent parent = null;
			
			try {
	            parent = FXMLLoader.load(getClass().getResource("zipcode.fxml"));
	         } catch (IOException e3) {
	            e3.printStackTrace();
	         }
	         
			Stage dialog = new Stage(StageStyle.UTILITY);
        	
        	dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.initOwner(btnFindzip.getScene().getWindow());
			dialog.setTitle("우편번호 검색");
			
			Scene scene = new Scene(parent);
			
			dialog.setScene(scene);
			dialog.setResizable(false); 
			dialog.show();

	        TableView<ZipcodeVO> tb_zipcode = (TableView<ZipcodeVO>) parent.lookup("#tb_zipcode");
	        TextField txtf_dong 			= (TextField) parent.lookup("#inputDong");
			Button btn_zip_ok 				= (Button) parent.lookup("#btn_zip_ok");
	        
	        tb_zipcode.setOnMouseClicked(event_pop->{
	        	
	            zip 	 = tb_zipcode.getSelectionModel().getSelectedItem().getZipcode();
	            gungu 	 = tb_zipcode.getSelectionModel().getSelectedItem().getGugun();
	            sido  	 = tb_zipcode.getSelectionModel().getSelectedItem().getSido();
	            dong  	 = tb_zipcode.getSelectionModel().getSelectedItem().getDong();
	            
	            btn_zip_ok.setOnAction(event_pop2->{
		        	
	            	inputZip.setText(zip);
		            inputAddr.setText(sido+" "+gungu+" "+dong);
		            dialog.close();
		            
		        });
	            
	         });
	        
	       
	        
		});

		btnCaptchachk.setOnAction(e -> {
			captchaCheck();
		}); 
		
		btnRefresh.setOnAction(e->{
			imgRefresh();
		});

		btnSubmit.setOnAction(e -> {	

			String mem_mail = inputEmail.getText();
			String mem_pw = inputPW.getText();
			String mem_pw2 = inputPW2.getText();

			if (mem_pw.equals(mem_pw2)) {
					pattern_cnt++;
					System.out.println("비밀번호 일치" + pattern_cnt);

			} else {
				errMsg("비밀번호 오류", " 입력하신 비밀번호가 일치하지 않습니다!");
				return;
			}

			/**
			 * 이름 정규식 한글만 가능 /^[가-힣]+$/
			 */

			String mem_name = inputName.getText().trim();
			Pattern p3 = Pattern.compile("^[가-힣]*$");
			Matcher m3 = p3.matcher(mem_name);
			result = m3.matches();

			if (result) { 
				pattern_cnt++;
				System.out.println("이름 정규화" + pattern_cnt);
			} else if (mem_name.equals(" ") || mem_name == null) {
				errMsg("에러!", "이름을 입력하지 않으셨습니다.");
				no_cnt++;
				return;
			} else if (result == false) {
				errMsg("에러!", "이름은 한글만 가능합니다.");
				return;
			}

			/**
			 * 생년월일 ex)941218 ==> '-' 쓰면 안됨 [0-9]{6}
			 */

			String mem_birth = inputBirth.getText();
			Pattern p4 = Pattern.compile("[0-9]{6}");
			Matcher m4 = p4.matcher(mem_birth);
			result = m4.matches();

			if (result) {
				pattern_cnt++;
				System.out.println("생년월일 정규화" + pattern_cnt);
			} else if (mem_birth.equals("") || mem_birth == null) {
				errMsg("에러!", "생년월일을 입력하지 않으셨습니다.");
				no_cnt++;
				return;
			} else if (result == false) {
				errMsg("에러", "생년월일을 형식에 맞게 입력하여 주십시오");
				return;
			}

			/**
			 * 핸드폰 번호 정규식 [0-9]{11}
			 */

			String mem_hp = inputHP.getText();

			Pattern p5 = Pattern.compile("[0-9]{11}");
			Matcher m5 = p5.matcher(mem_hp);
			result = m5.matches();

			if (result) {
				pattern_cnt++;
				System.out.println("핸드폰 정규화" + pattern_cnt);
			} else if (mem_hp.equals("") || mem_hp == null) {
				errMsg("에러!", "핸드폰 번호를 입력하지 않으셨습니다!");
				no_cnt++;
				return;
			} else if (result == false) {
				errMsg("에러!", "핸드폰 번호를 입렬할 땐 '-' 을 사용하지 말아주세요.");
				return;
			}

			String mem_nick = inputNick.getText();
			Pattern p6 = Pattern.compile("^[가-힣]*$");
			Matcher m6 = p6.matcher(mem_nick);
			result = m6.matches();

			if (result) {
				pattern_cnt++;
				System.out.println("닉네임 정규화" + pattern_cnt);
			} else {
				errMsg("에러!", "닉네임은 한글만 사용 할 수 있습니다.");
				return;
			}
			
			
			String mem_zipcode = inputZip.getText();
			if (mem_zipcode.equals("") || mem_zipcode == null) {
				errMsg("에러!", "우편번호를 입력하지 않으셨습니다");
				no_cnt++;
				return;
			}
			
			String mem_zip = inputZip.getText();
			if (mem_zip.equals("") || mem_zip == null) {
				errMsg("에러!", "우편번호를 입력하지 않으셨습니다");
				no_cnt++;
				return;
			}

			if (chk_cnt == 3 && pattern_cnt == 7 && no_cnt == 0) {
				
				int cnt = 0;
				mvo.setMem_mail(mem_mail);
				mvo.setMem_pw(mem_pw);
				mvo.setMem_name(mem_name);
				mvo.setMem_birth(mem_birth);
				mvo.setMem_phone(mem_hp);
				mvo.setMem_nick(mem_nick);
				mvo.setMem_addr1(inputZip.getText());
				mvo.setMem_addr2(inputAddr.getText());

				try {
					cnt = ijs.insertMember(mvo);
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}

				if (cnt > 0) {
					infoMsg("회원가입 성공", "어서오세요! 챠니네 오까게입니다 :) ");
					 Stage stage = (Stage) btnSubmit.getScene().getWindow();
					 stage.close();
				} else {
					errMsg("회원가입 실패 ㅠㅠ", "회원가입에 문제가 있습니다. :( ");
				}

			}else {
				errMsg("회원가입 실패", "입력하신 정보 중 올바르지 않은 정보가 있습니다. 아이디, 닉네임, 캡차 확인 버튼을 다시 눌러주세요");
				chk_cnt = 0;
				pattern_cnt = 0;
				no_cnt = 0;
			}
		});
		
		btnCancel.setOnAction(e->{
			Stage stage = (Stage) btnCancel.getScene().getWindow();
			 stage.close();
		});
	}
	
	
	public void captchaCheck() {
		
		String result1 = captchaResult(captchaKey, inputCaptcha.getText());
		if(inputCaptcha.getText().equals("")) {
			captchaFlag = false;
			errMsg("에러!", "좌측에 보이는 이미지의 문자를 입력해주세요");
		}else if(result1.equals("false")) {
			imgRefresh();
			captchaFlag = false;
			errMsg("에러!", "좌측에 보이는 이미지의 문자와 입력하신 문자가 다릅니다!");
		}else if(result1.equals("true,")) {
			captchaFlag = true;
			infoMsg("성공!", "일치합니다!");
			chk_cnt++;
		}
	}
	
	public void captchaImage(String CaptchaKey) {
        String clientId = "TUjLAVWKUEIvb4vZQerA";//애플리케이션 클라이언트 아이디값";
        String clientSecret = "2fxGOTWaSL";//애플리케이션 클라이언트 시크릿값";
        try {
            String key = CaptchaKey; // https://openapi.naver.com/v1/captcha/nkey 호출로 받은 키값
            String apiURL = "https://openapi.naver.com/v1/captcha/ncaptcha.bin?key=" + key;
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode==200) { // 정상 호출
                InputStream is = con.getInputStream();
                int read = 0;
                byte[] bytes = new byte[1024];
                // 랜덤한 이름으로 파일 생성
                captchaImg = Long.valueOf(new Date().getTime()).toString();
                // 파일 저장위치 변경해보자 (d:/D_Other/복사본_Tulips.jpg)
                // join패키지에 넣자 일단.
                f = new File("captchaImg.jpg");
                f.createNewFile();
                OutputStream outputStream = new FileOutputStream(f);
                while ((read =is.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
                is.close();
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();
                System.out.println(response.toString());
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        
		Image captcha = new Image("file:"+f.getAbsolutePath());
		//img_auto.setImage(captcha);
	}
	
	public String captchaKey() {
        String clientId = "TUjLAVWKUEIvb4vZQerA";//애플리케이션 클라이언트 아이디값";
        String clientSecret = "2fxGOTWaSL";//애플리케이션 클라이언트 시크릿값";
        String result = "";
        try {
            String code = "0"; // 키 발급시 0,  캡차 이미지 비교시 1로 세팅
            String apiURL = "https://openapi.naver.com/v1/captcha/nkey?code=" + code;
            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode==200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            System.out.println(response.toString());
            result = response.toString().split("\"")[3];
        } catch (Exception e) {
            System.out.println(e);
        }

		return result;
	}
	
	public String captchaResult(String CaptchaKey, String input) {
        String clientId = "TUjLAVWKUEIvb4vZQerA";//애플리케이션 클라이언트 아이디값";
        String clientSecret = "2fxGOTWaSL";//애플리케이션 클라이언트 시크릿값";
        String result = "";
        try {
            String code = "1"; // 키 발급시 0,  캡차 이미지 비교시 1로 세팅
            String key = CaptchaKey; // 캡차 키 발급시 받은 키값
            String value = input; // 사용자가 입력한 캡차 이미지 글자값
            String apiURL = "https://openapi.naver.com/v1/captcha/nkey?code=" + code +"&key="+ key + "&value="+ value;

            URL url = new URL(apiURL);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("X-Naver-Client-Id", clientId);
            con.setRequestProperty("X-Naver-Client-Secret", clientSecret);
            int responseCode = con.getResponseCode();
            BufferedReader br;
            if(responseCode==200) { // 정상 호출
                br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            } else {  // 에러 발생
                br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
            }
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = br.readLine()) != null) {
                response.append(inputLine);
            }
            br.close();
            System.out.println(response.toString());
            result = response.toString().substring(10, 15);
        } catch (Exception e) {
            System.out.println(e);
        }
		return result;
	}
	
	public void imgRefresh() {
		captchaKey = captchaKey();
		captchaImage(captchaKey);
	}
	
	private void errMsg(String headerText, String msg) {
		Alert errAlert = new Alert(AlertType.ERROR);
		errAlert.setTitle("오류");
		errAlert.setHeaderText(headerText);
		errAlert.setContentText(msg);
		errAlert.showAndWait();
	}

	private void infoMsg(String headerText, String msg) {
		Alert errAlert = new Alert(AlertType.INFORMATION);
		errAlert.setTitle("MelonPlate Id 찾기");
		errAlert.setHeaderText(headerText);
		errAlert.setContentText(msg);
		errAlert.showAndWait();
	}

}
