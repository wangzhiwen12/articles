package com.wfj.member.sdk.client;

import com.wfj.member.sdk.common.*;
import com.wfj.member.sdk.utils.*;
import com.wfj.member.sdk.utils.FieldsAnnoatation.InterfaceBeanUtil;
import net.sf.json.JSONObject;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by MaYong on 2015/11/25.
 */
public class MemberClient {

    /**
     * 把object转成json，加密传输
     *
     * @param method
     * @param object
     * @return
     * @throws Exception
     */
    public static String post(String controllerMethod, String method, Object object) throws Exception {
        return post(controllerMethod, method, object, 30000);
    }
    /**
     * 
     * @param methodName	方法名称
     * @param param			传递的参数
     * @return
     */
    public static MsgReturnDto postMember(String methodName, String param) {
    	
    	MsgReturnDto dto = new MsgReturnDto();
    	dto.setCode("0");
    	// 1.	参数判断。
    	if (StringUtils.isBlank(methodName)) {
    		dto.setDesc("参数 methodName 不能为空");
    		return dto;
		}
    	if (StringUtils.isBlank(param)) {
    		dto.setDesc("参数 param 不能为空");
    		return dto;
    	}
    	// 2.	 请求后台。
    	String[] stringArray = methodName.split("\\.");
    	String method = null;
    	String caseMethod = null;
    	try {
    		method = stringArray[0];
    		caseMethod = stringArray[1];
		} catch (Exception e) {
			dto.setDesc("methodName 不合法!");
			return dto;
		}
		try {
			String res = post(method, caseMethod, param, 30000);
			dto = JsonUtils.jsonToBean2(res, MsgReturnDto.class);
			return dto;
		} catch (Exception e) {
			e.printStackTrace();
			dto.setDesc(e.getMessage());
		}
        return dto;
    }

    /**
     * 把object转成json，加密传输
     *
     * @param method
     * @param object
     * @param timeoutSecond
     * @return
     * @throws Exception
     */
    public static String post(String controllerMethod, String method, Object object, int timeoutSecond) throws Exception {
        String string = null;
        if (object instanceof String) {
            string = (String) object;
        } else {
            string = JsonUtils.beanToJson(object);
        }
        String miwenString = AESUtils.encryptString(string, Config.getAesKey());
        DataDto dataDto = new DataDto();
        dataDto.setData(miwenString);
        dataDto.setDomainName(Config.getDomainName());
        dataDto.setIp(Config.getIp());
        dataDto.setMethod(method);
        String sign = DSAUtils.sign(miwenString);
        dataDto.setSign(sign);
        String url = Config.getMemberUrl() + controllerMethod + ".do";
        String data = JsonUtils.beanToJson(dataDto);
        String msg = HttpUtil.postBody(url, data, timeoutSecond);
        return msg;
    }

    /**
     * 根据SID查询会员信息
     *
     * @param memberSid
     * @return 会员全部数据结构
     */
    public static MsgReturnDto findByMemberSid(Long memberSid) {
        MsgReturnDto dto = new MsgReturnDto();
        if (memberSid == null) {
            dto.setCode("0");
            dto.setDesc("参数不能为空");
            return dto;
        }
        return null;
    }

    /**
     * 校验登录状态
     *
     * @param ticket 登录后的ticket，取自cookie
     * @return MsgReturnDto信息
     */
    public static MsgReturnDto valLoginStatus(String ticket) {
        MsgReturnDto dto = new MsgReturnDto();
        if (ticket == null || ticket.equals("")) {
            dto.setCode("2");
            dto.setDesc("未登录");
            return dto;
        }
        LoginDto loginDto = new LoginDto();
        loginDto.setTicket(ticket);
        try {
            String jsonStr = post("inbound", "valLoginStatus", loginDto);
            if (jsonStr == null || jsonStr.equals("")) {
                dto.setCode("2");
                dto.setDesc("系统异常");
                return dto;
            }
            dto = JsonUtils.jsonToBean2(jsonStr, MsgReturnDto.class);
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            dto.setCode("0");
            dto.setDesc(e.getMessage());
            return dto;
        }
    }

    /**
     * 校验登录状态
     *
     * @param request request
     * @param uid     用户ID
     * @return MsgReturnDto信息
     */
    public static MsgReturnDto valLoginStatus(HttpServletRequest request, String uid) {
        MsgReturnDto dto = valLoginStatus(request);
        if (!dto.getCode().equals("1")) {
            return dto;
        }
        Map object = (HashMap) dto.getObject();
        String sid = object.get("sid").toString();
        if (uid.equals(sid)) {
            return dto;
        } else {
            MsgReturnDto returnDto = new MsgReturnDto();
            returnDto.setCode("0");
            returnDto.setDesc("未登录");
            return returnDto;
        }
    }

    /**
     * 校验登录状态
     *
     * @param request request
     * @return MsgReturnDto信息
     */
    public static MsgReturnDto valLoginStatus(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        MsgReturnDto dto = new MsgReturnDto();
        if (cookies == null || cookies.length == 0) {
            dto.setCode("0");
            dto.setDesc("未登录");
            return dto;
        }
        String ticket = null;
        for (int i = 0; i < cookies.length; i++) {
            String cookieKey = cookies[i].getName();
            if ("ticket".equals(cookieKey)) {
                ticket = cookies[i].getValue();
                break;
            }
        }
        if (ticket == null) {
            dto.setCode("0");
            dto.setDesc("未登录");
            return dto;
        }
        dto = valLoginStatus(ticket);
        return dto;
    }

    /**
     * 获取登录验证字串
     *
     * @param returnUrl 登录后跳转地址
     * @return 加密后的字串
     */
    public static DataDto getLoginSignStr(String returnUrl)
            throws RuntimeException {
        LoginDto loginDto = new LoginDto();
        loginDto.setReturnUrl(returnUrl);
        String msg = JsonUtils.beanToJson(loginDto);
        msg = AESUtils.encryptString(msg, Config.getAesKey());
        DataDto dataDto = new DataDto();
        msg = URLEncoder.encode(msg);
        dataDto.setData(msg);
        String sign = DSAUtils.sign(msg);
        sign = URLEncoder.encode(sign);
        dataDto.setSign(sign);
        dataDto.setDomainName(Config.getDomainName());
        dataDto.setIp(Config.getIp());
        return dataDto;
    }

    public static String getMailSignStr(String dataStr){
        String msg = AESUtils.encryptString(dataStr, Config.getAesKey());
        String sign = DSAUtils.sign(msg);
        sign = URLEncoder.encode(sign);
        StringBuffer sb = new StringBuffer();
        sb.append(dataStr);
        sb.append("&sign=");
        sb.append(sign);
        return sb.toString();

    }

    /**
     * 获取注册验证字串
     *
     * @param returnUrl 登录后跳转地址
     * @return 加密后的字串
     */
    public static DataDto getRegisterSignStr(String returnUrl)
            throws RuntimeException {
        LoginDto loginDto = new LoginDto();
        loginDto.setReturnUrl(returnUrl);
        String msg = JsonUtils.beanToJson(loginDto);
        msg = AESUtils.encryptString(msg, Config.getAesKey());
        DataDto dataDto = new DataDto();
        msg = URLEncoder.encode(msg);
        dataDto.setData(msg);
        String sign = DSAUtils.sign(msg);
        sign = URLEncoder.encode(sign);
        dataDto.setSign(sign);
        dataDto.setDomainName(Config.getDomainName());
        dataDto.setIp(Config.getIp());
        return dataDto;
    }

    /**
     * 获取登录退出验证字串
     *
     * @param returnUrl 退出后跳转地址
     * @return 加密后的字串
     */
    public static DataDto getLogoutSignStr(String returnUrl, String ticket)
            throws RuntimeException {
        LoginDto loginDto = new LoginDto();
        loginDto.setReturnUrl(returnUrl);
        loginDto.setTicket(ticket);
        String msg = JsonUtils.beanToJson(loginDto);
        msg = AESUtils.encryptString(msg, Config.getAesKey());
        DataDto dataDto = new DataDto();
        msg = URLEncoder.encode(msg);
        dataDto.setData(msg);
        String sign = DSAUtils.sign(msg);
        sign = URLEncoder.encode(sign);
        dataDto.setSign(sign);
        dataDto.setDomainName(Config.getDomainName());
        dataDto.setIp(Config.getIp());
        return dataDto;
    }

    private MsgReturnDto getReturnByJson(String json) {
        JSONObject jsonObject = JSONObject.fromObject(json);
        MsgReturnDto returnDto = new MsgReturnDto();
        returnDto.setCode(jsonObject.getString("code"));
        returnDto.setDesc(jsonObject.getString("desc"));
        returnDto.setObject(jsonObject.getString("data"));
        return returnDto;
    }

    /**
     * 根据ticket取地址列表
     *
     * @param ticket
     * @return
     */
    public static MsgReturnDto getAddressList(String ticket) {
        LoginDto loginDto = new LoginDto();
        loginDto.setTicket(ticket);
        try {
            String jsonStr = post("inbound", "getAddressList", loginDto);
            MsgReturnDto dto = JsonUtils.jsonToBean2(jsonStr, MsgReturnDto.class);
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            MsgReturnDto dto = new MsgReturnDto();
            dto.setCode("0");
            dto.setDesc(e.getMessage());
            return dto;
        }

    }
    /**
     * 根据 ticket 获取用户默认收货地址
     * @param ticket
     * @return
     */
    public static MsgReturnDto getDefaultAddress(String ticket) {
    	LoginDto loginDto = new LoginDto();
    	loginDto.setTicket(ticket);
    	try {
			String jsonStr = post("inbound", "getDefaultAddress", loginDto);
			MsgReturnDto dto = JsonUtils.jsonToBean2(jsonStr, MsgReturnDto.class);
			return dto;
		} catch (Exception e) {
			e.printStackTrace();
            MsgReturnDto dto = new MsgReturnDto();
            dto.setCode("0");
            dto.setDesc(e.getMessage());
            return dto;
		}
    }

    /**
     * 增加收货地址
     *
     * @param memberAddressDto
     * @return
     */
    public static MsgReturnDto insertAddress(MemberAddressDto memberAddressDto) {
        memberAddressDto.setSid(null);
        String msg = null;
        try {
            msg = InterfaceBeanUtil.val(memberAddressDto);
        } catch (Exception e) {
            e.printStackTrace();
            MsgReturnDto dto = new MsgReturnDto();
            dto.setCode("0");
            dto.setDesc(e.getMessage());
            return dto;
        }
        if (msg != null) {
            MsgReturnDto dto = new MsgReturnDto();
            dto.setCode("0");
            dto.setDesc(msg);
            return dto;
        }
        try {
            String jsonStr = post("addressInbound", "saveOrUpdateAddress", memberAddressDto);
            MsgReturnDto dto = JsonUtils.jsonToBean2(jsonStr, MsgReturnDto.class);
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            MsgReturnDto dto = new MsgReturnDto();
            dto.setCode("0");
            dto.setDesc(e.getMessage());
            return dto;
        }
    }

    /**
     * 修改收货地址
     *
     * @param memberAddressDto
     * @return
     */
    public static MsgReturnDto updateAddress(MemberAddressDto memberAddressDto) {
        if (memberAddressDto.getSid() == null) {
            MsgReturnDto dto = new MsgReturnDto();
            dto.setCode("0");
            dto.setDesc("SID不能为空");
            return dto;
        }
        String msg = null;
        try {
            msg = InterfaceBeanUtil.val(memberAddressDto);
        } catch (Exception e) {
            e.printStackTrace();
            MsgReturnDto dto = new MsgReturnDto();
            dto.setCode("0");
            dto.setDesc(e.getMessage());
            return dto;
        }
        if (msg != null) {
            MsgReturnDto dto = new MsgReturnDto();
            dto.setCode("0");
            dto.setDesc(msg);
            return dto;
        }
        try {
            String jsonStr = post("addressInbound", "saveOrUpdateAddress", memberAddressDto);
            MsgReturnDto dto = JsonUtils.jsonToBean2(jsonStr, MsgReturnDto.class);
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            MsgReturnDto dto = new MsgReturnDto();
            dto.setCode("0");
            dto.setDesc(e.getMessage());
            return dto;
        }
    }

    /**
     * 取省级区划
     *
     * @param
     * @return
     */
    public static MsgReturnDto getProvince() {
        try {
            String jsonStr = post("getXZQH", "getSheng", "111");
            MsgReturnDto dto = JsonUtils.jsonToBean2(jsonStr, MsgReturnDto.class);
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            MsgReturnDto dto = new MsgReturnDto();
            dto.setCode("0");
            dto.setDesc(e.getMessage());
            return dto;
        }
    }

    /**
     * 取市级区划
     *
     * @param provinceSid 省级区划SID
     * @return
     */
    public static MsgReturnDto getCity(String provinceSid) {
        try {
            String jsonStr = post("getXZQH", "getShi", provinceSid);
            MsgReturnDto dto = JsonUtils.jsonToBean2(jsonStr, MsgReturnDto.class);
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            MsgReturnDto dto = new MsgReturnDto();
            dto.setCode("0");
            dto.setDesc(e.getMessage());
            return dto;
        }
    }

    /**
     * 取县区级区划
     *
     * @param citySid 市级区划SID
     * @return
     */
    public static MsgReturnDto getArea(String citySid) {
        try {
            String jsonStr = post("getXZQH", "getXian", citySid);
            MsgReturnDto dto = JsonUtils.jsonToBean2(jsonStr, MsgReturnDto.class);
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            MsgReturnDto dto = new MsgReturnDto();
            dto.setCode("0");
            dto.setDesc(e.getMessage());
            return dto;
        }
    }

    /**
     * 根据UID，也就是会员主键取发票信息列表
     *
     * @param uid  uid
     * @param type 发票类型
     * @return
     */
    public static MsgReturnDto getInvoiceList(Long uid, Integer type) {
        MemberInvoiceDto invoiceDto = new MemberInvoiceDto();
        invoiceDto.setMemberSid(uid);
        invoiceDto.setInvoiceType(type);
        try {
            String jsonStr = post("getInvoiceInfo", "getInvoiceList", invoiceDto);
            MsgReturnDto dto = JsonUtils.jsonToBean2(jsonStr, MsgReturnDto.class);
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            MsgReturnDto dto = new MsgReturnDto();
            dto.setCode("0");
            dto.setDesc(e.getMessage());
            return dto;
        }

    }

    /**
     * 取发票类别 明细、办公用品、劳保用品等
     *
     * @return
     */
    public static MsgReturnDto getInvoiceType() {
        try {
            MemberInvoiceDto invoiceDto = new MemberInvoiceDto();
            String jsonStr = post("getInvoiceInfo", "getInvoiceType", invoiceDto);
            MsgReturnDto dto = JsonUtils.jsonToBean2(jsonStr, MsgReturnDto.class);
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            MsgReturnDto dto = new MsgReturnDto();
            dto.setCode("0");
            dto.setDesc(e.getMessage());
            return dto;
        }
    }

    /**
     * 增加发票信息
     *
     * @param invoiceDto 发票信息
     * @return
     */
    public static MsgReturnDto addInvoice(MemberInvoiceDto invoiceDto) {
        invoiceDto.setSid(null);
        try {
            String jsonStr = post("getInvoiceInfo", "addInvoice", invoiceDto);
            MsgReturnDto dto = JsonUtils.jsonToBean2(jsonStr, MsgReturnDto.class);
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            MsgReturnDto dto = new MsgReturnDto();
            dto.setCode("0");
            dto.setDesc(e.getMessage());
            return dto;
        }
    }

    /**
     * 修改发票信息
     *
     * @param invoiceDto 发票信息
     * @return
     */
    public static MsgReturnDto updateInvoice(MemberInvoiceDto invoiceDto) {
        try {
            String jsonStr = post("getInvoiceInfo", "updateInvoice", invoiceDto);
            MsgReturnDto dto = JsonUtils.jsonToBean2(jsonStr, MsgReturnDto.class);
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            MsgReturnDto dto = new MsgReturnDto();
            dto.setCode("0");
            dto.setDesc(e.getMessage());
            return dto;
        }

    }

    /**
     * 增加关注
     *
     * @param memberCollectDto 关注信息
     * @return
     */
    public static MsgReturnDto addMemberCollect(MemberCollectDto memberCollectDto) {
        String msg = null;
        try {
            msg = InterfaceBeanUtil.val(memberCollectDto);
        } catch (Exception e) {
            e.printStackTrace();
            msg = e.getMessage();
        }
        if (msg != null) {
            MsgReturnDto returnDto = new MsgReturnDto();
            returnDto.setCode("0");
            returnDto.setDesc(msg);
            return returnDto;
        }
        try {
            String jsonStr = post("memberCollect", "addMemberCollect", memberCollectDto);
            MsgReturnDto dto = JsonUtils.jsonToBean2(jsonStr, MsgReturnDto.class);
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            MsgReturnDto dto = new MsgReturnDto();
            dto.setCode("0");
            dto.setDesc(e.getMessage());
            return dto;
        }

    }
    /**
     * 增加一组关注
     *
     * @param collectList 关注信息
     * @return
     */
    public static MsgReturnDto addMemberCollect(List<MemberCollectDto> collectList) {
    	// 1.   判断数组是否为空
    	if (collectList.isEmpty()) {
    		MsgReturnDto returnDto = new MsgReturnDto();
            returnDto.setCode("0");
            returnDto.setDesc("数组不能为空!");
            return returnDto;
		}
    	
    	// 2.   声明返回结果集
    	List<MsgReturnDto> resList = new ArrayList<>();
    	
    	// 3.   循环调用单次关注接口
    	for (int i = 0; i < collectList.size(); i++) {
    		String msg = null;
    		MemberCollectDto memberCollectDto = null;
            try {
        		memberCollectDto = collectList.get(i);
        		msg = InterfaceBeanUtil.val(memberCollectDto);
            } catch (Exception e) {
                e.printStackTrace();
                msg = e.getMessage();
            }
            if (msg != null) {
            	MsgReturnDto returnDto = new MsgReturnDto();
                returnDto.setCode("0");
                returnDto.setDesc(msg);
                resList.add(returnDto);
                continue;
            }
            
            try {
            	MemberCollectDto memberCollect = collectList.get(i);
                String jsonStr = post("memberCollect", "addMemberCollect", memberCollect);
                MsgReturnDto dto = JsonUtils.jsonToBean2(jsonStr, MsgReturnDto.class);
                resList.add(dto);
            } catch (Exception e) {
                e.printStackTrace();
                MsgReturnDto dto = new MsgReturnDto();
                dto.setCode("0");
                dto.setDesc(e.getMessage());
                resList.add(dto);
            }
		}
    	
    	// 4.   处理返回结果
    	MsgReturnDto resDto = new MsgReturnDto();
    	
    	List<String> trueList = new ArrayList<>();
    	List<String> falseList = new ArrayList<>();
    	
    	for (int i = 0; i < resList.size(); i++) {
    		MsgReturnDto dto = resList.get(i);
    		MemberCollectDto memberCollectDto = collectList.get(i);
    		if ("1".equals(dto.getCode())) {
    			trueList.add(memberCollectDto.getContentSid());
			} else {
				falseList.add(memberCollectDto.getContentSid());
			}
		}
    	if (!trueList.isEmpty()) {
    		resDto.setCode("1");
		} else {
			resDto.setCode("0");
		}
    	JSONObject jsonObject = new JSONObject();
    	jsonObject.put("trueList", trueList);
    	jsonObject.put("falseList", falseList);
    	resDto.setObject(jsonObject.toString());
    	resDto.setDesc("成功关注列表：trueList；失败关注列表：falseList, falseList 中有null的时候，请检查传递的contentSid");
        return resDto;
    }

    /**
     * 删除关注
     *
     * @param sid 关注信息SID
     * @return
     */
    public static MsgReturnDto delMemberCollect(Long sid) {
        MemberCollectDto collectDto = new MemberCollectDto();
        collectDto.setSid(sid);
        try {
            String jsonStr = post("memberCollect", "delMemberCollect", collectDto);
            MsgReturnDto dto = JsonUtils.jsonToBean2(jsonStr, MsgReturnDto.class);
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            MsgReturnDto dto = new MsgReturnDto();
            dto.setCode("0");
            dto.setDesc(e.getMessage());
            return dto;
        }

    }
    
    /**
     * 快速登陆
     * @param emailOrMobile	用户登陆账号
     * @param password		用户登陆密码
     * @param request		HttpServletRequest, 用于获取用户浏览器信息
     * @return
     */
    public static MsgReturnDto quickLogin(String emailOrMobile, String password, HttpServletRequest request) {
    	MsgReturnDto dto = new MsgReturnDto();
    	String code = "0";
    	String desc = "账号或密码错误";
    	if (!StringUtils.isBlank(emailOrMobile) || !StringUtils.isBlank(password)) {
			Map<String, Object> map = new HashMap<>();
			map.put("emailOrMobile", emailOrMobile);
			map.put("password", password);
			
			String browser = request.getHeader("User-agent");
			map.put("browser", browser);
			
			String resultStr = null;
			try {
				resultStr = post("quickLogin", null, map);
				if (StringUtils.isNotBlank(resultStr)) {
					dto = JsonUtils.jsonToBean(resultStr, MsgReturnDto.class);
					return dto;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    	dto.setCode(code);
    	dto.setDesc(desc);
    	return dto;
    }
    /**
     * 根据  uid 获取 cid
     * @return
     */
    public static MsgReturnDto getCidByUid(String uid) {
    	MsgReturnDto dto = new MsgReturnDto();
    	String code = "0";
    	String desc = "uid不能为空!";
    	try {
			Long.valueOf(uid);
		} catch (Exception e) {
	    	dto.setDesc(e.getMessage());
	    	return dto;
		}
    	if (StringUtils.isNotBlank(uid)) {
    		 try {
				String jsonStr = post("getCidByUid", null, uid);
				JSONObject jsonObject = JSONObject.fromObject(jsonStr);
				String cid = "";
				if (!jsonObject.isNullObject()) {
					if (jsonObject.has("object")) {
						cid = jsonObject.getString("object");
					}
				}
				dto.setObject(cid);
				code = "1";
				desc = "";
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    	dto.setCode(code);
    	dto.setDesc(desc);
    	return dto;
    }
    /**
     * 查询关注总数
     * @param sku
     * @return
     */
    public static MsgReturnDto getMemberCollectCount(String sku) {
    	MsgReturnDto dto = new MsgReturnDto();
    	dto.setCode("0");
		try {
			if (StringUtils.isNotBlank(sku)) {
				String res = post("getMemberCollectCount", null, sku);
				dto = JsonUtils.jsonToBean(res, MsgReturnDto.class);
				return dto;
			} else {
				dto.setDesc("参数 sku 不存在!");
			}
		} catch (Exception e) {
			dto.setCode("1");
			dto.setDesc(e.toString());
		}
    	return dto;
    }
    /**
	 * CMS商品判断关注接口
	 * @return
	 */
    public static MsgReturnDto cmsMemberCollect(MemberCollectDto collectDto) {
    	String msg = null;
    	MsgReturnDto dto = new MsgReturnDto();
    	dto.setCode("0");
		try {
			msg = InterfaceBeanUtil.val(collectDto);
			if (msg != null) {
				dto.setDesc(msg);
				return dto;
			}
			
			String res = post("cmsMemberCollect", null, collectDto);
			dto = JsonUtils.jsonToBean(res, MsgReturnDto.class);
			return dto;
		} catch (Exception e) {
			dto.setDesc(e.toString());
		}
    	return dto;
    }
    /**
     * 查询关注列表 （分页查询）
     * @param collectPageDto
     * @return
     */
    public static MsgReturnDto getMemberCollectPage(CollectPageDto collectPageDto) {
    	MsgReturnDto dto = new MsgReturnDto();
    	dto.setCode("0");
    	String msg = null;
    	try {
    		msg = InterfaceBeanUtil.val(collectPageDto);
			if (msg != null) {
				dto.setDesc(msg);
				return dto;
			}
			
			String res = post("getMemberCollectPage", null, collectPageDto);
			dto = JsonUtils.jsonToBean(res, MsgReturnDto.class);
			return dto;
		} catch (Exception e) {
			dto.setDesc(e.toString());
			return dto;
		}
    }
    /**
     * 根据 id 查询用户信息
     * @param id		
     * @param type		cid / uid
     * @return
     */
    public static MsgReturnDto getMemberInfoByCidOrUid(String id, String type) {
    	MsgReturnDto dto = new MsgReturnDto();
    	dto.setCode("0");
    	try {
	    	if (StringUtils.isNotBlank(id) && StringUtils.isNotBlank(type)) {
	    		Map<String, Object> paramMap = new HashMap<>();
				String typeStr = type.toLowerCase();
				if (typeStr.equals("cid")) {
					paramMap.put("type", "cid");
				} else {
					paramMap.put("type", "uid");
				}
				paramMap.put("id", id);
				String res = post("getMemberInfoByCidOrUid", null, paramMap);
				dto = JsonUtils.jsonToBean(res, MsgReturnDto.class);
				return dto;
			}
    	} catch (Exception e) {
    		dto.setCode("0");
    		dto.setDesc(e.toString());
		}
    	return dto;
    }
}
