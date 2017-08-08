// pro2_1.0.cpp : 定义控制台应用程序的入口点。
//WMI提供的API是基于COM的，所以必须首先执行CoInitializeEx 和 CoInitializeSecurity函数，以便访问WMI。

#include "stdafx.h"

#define _WIN32_DCOM
//CoInitializeEx处理标志

#include <iostream>
//#include <iostream.h>
using namespace std;
#include <comdef.h>
#include <Wbemidl.h>
# pragma comment(lib, "wbemuuid.lib")

#include<stdio.h>
#include<stdlib.h>




int main(int argc, char **argv)
{
    HRESULT hres;
    // Step 1: --------------------------------------------------
    //CoInitializeEx是 Windows提供的API函数，
	//用CoInitializeEx函数初始化COM接口：
	//------------------------------------------
    hres =  CoInitializeEx(0, COINIT_MULTITHREADED);
	
	//if(SUCCEEDED(hres))
   if (FAILED(hres))
    {
        cout << "Step1 failed. Error code = 0x" 
            << hex << hres << endl; //hres = 0
        return 1;                  // Program has failed.
    }
	
	
     // Step 2: --------------------------------------------------
	//用CoInitializeSecurity函数注册并设置进程的默认的安全值：
	//参数CoInitializeSecurity - - - - - - - - - - - - - - - - - - - - - - - 
       hres =  CoInitializeSecurity(
        NULL, 						  // 取NULL COM生成允许任何人访问 有三个指针，对应多种方式
        -1,                          // COM要注册认证服务  如果此参数为0，则不会注册认证服务，并且服务器无法接收安全呼叫。
        NULL,                        // 身份验证服务（数组）  有关详细信息，请参阅SOLE_AUTHENTICATION_SERVICE。
        NULL,                        // 保留，必须为NULL
        RPC_C_AUTHN_LEVEL_DEFAULT,   // 默认身份验证
        RPC_C_IMP_LEVEL_IMPERSONATE, // 默认模拟  
        NULL,                        // 认证信息 是一个SOLE_AUTHENTICATION_INFO结构的数组。此列表指示客户机可用于调用服务器的每个认证服务的信息
        EOAC_NONE,                   // 附加功能 暂不添加
        NULL                         // 保留 必须为NULL
        );
		
	if (FAILED(hres))
    {
        cout << "Step2 failed. Error code = 0x" 
            << hex << hres << endl;
        CoUninitialize();
        return 1;                    // Program has failed.
    }
    
       // Step 3: ---------------------------------------------------
    // 创建一个到WMI命名空间的连接 WMI并不是运行在我们自己的进程中，需要在WMI和我们的程序中创建一个连接。 
    IWbemLocator *pLoc = NULL;
    hres = CoCreateInstance(
        CLSID_WbemLocator, //创建的Com对象的类标识符(CLSID)
		// CLSID是指windows系统对于不同的应用程序，对其身份的标示和与其他对象进行区分。
		
        NULL, 
		
        CLSCTX_INPROC_SERVER, //CLSCTX_INPROC_SERVER值告诉CoCreateInstance只装载包含进程中服务器或DLL中的组件。
		
        IID_IWbemLocator, //创建的Com对象的接口标识符
		
		(LPVOID *) &pLoc  //用来接收指向Com对象接口地址的指针变量
		
		);
 
    if (FAILED(hres))
    {
        cout << "Step3 failed."
            << " Err code = 0x"
            << hex << hres << endl;
        CoUninitialize();
        return 1;                 // Program has failed.
    }
	
	
	
    // Step 4: -----------------------------------------------------
    // 用IWbemLocator::ConnectServer方法连接到WMI
	//ConnectServer方法返回一个IWbemServices接口的代理，可以用来访问本地或是远程WMI命名空间。
	//通过连接到WMI        IWbemLocator::ConnectServer方法
    IWbemServices *pSvc = NULL;

    hres = pLoc->ConnectServer(
         _bstr_t(L"ROOT\\CIMV2"), // 对象路径的WMI名称空间
	//这个命名空间里包括了绝大多数与计算机、操作系统相关联的类。
         NULL,                    // 用户为空
         NULL,                    // 密码为空
         0,                       // 当前地址为空
         NULL,                    // 安全标志
         0,                       // 权限
         0,                       // Context对象
         &pSvc                    // 指向IWbemServices代理
         );
    
    if (FAILED(hres))
    {
        cout << "Step4 failed. Error code = 0x" 
             << hex << hres << endl;
        pLoc->Release();     
        CoUninitialize();
        return 1;                // Program has failed.
    }
    cout << "Connected to ROOT\\CIMV2 WMI namespace" << endl;
	
	
// Step 5: --------------------------------------------------
    // 设置WMI连接的安全属性
	//因为IWbemServices代理允许使用进程外对象，但是在COM中，如果没有设置安全属性，是不允许进程间互相访问的。
    hres = CoSetProxyBlanket(
       pSvc,                        // 代理设置
       RPC_C_AUTHN_WINNT,           // RPC_C_AUTHN_xxx
       RPC_C_AUTHZ_NONE,            // RPC_C_AUTHZ_xxx
       NULL,                        // 服务器名
       RPC_C_AUTHN_LEVEL_CALL,      // RPC_C_AUTHN_LEVEL_xxx 
       RPC_C_IMP_LEVEL_IMPERSONATE, // RPC_C_IMP_LEVEL_xxx
       NULL,                        // 客户端名
       EOAC_NONE                    // 代理功能
    );
    if (FAILED(hres))
    {
        cout << "Could not set proxy blanket. Error code = 0x" 
            << hex << hres << endl;
        pSvc->Release();
        pLoc->Release();     
        CoUninitialize();
        return 1;               // Program has failed.
    }
	
	
	
	
	
    // Step 6: 按需求操作
    //用IWbemServices指针向WMI发送请求，获取Win32_BIOS类的实例集合
	//自己按要求操作    例如，获取操作系统的名称
	//·遍历IEnumWbemClassObject，输出信息
    IEnumWbemClassObject* pEnumerator = NULL;
    hres = pSvc->ExecQuery(
        bstr_t("WQL"), 
        bstr_t("SELECT * FROM Win32_PhysicalMedia"),
        WBEM_FLAG_FORWARD_ONLY | WBEM_FLAG_RETURN_IMMEDIATELY, 
        NULL,
        &pEnumerator);
    
	
	//COM是使用其特定的数据类型，字符串 _bstr_t是其中一种源类型
    if (FAILED(hres))
    {
        cout << "Query for physical media failed."
            << " Error code = 0x" 
            << hex << hres << endl;
        pSvc->Release();
        pLoc->Release();
        CoUninitialize();
        return 1;               // Program has failed.
    }
	
    // Step 7: -------------------------------------------------
    // 从第6步中获取查询的数据
    IWbemClassObject *pclsObj;
    ULONG uReturn = 0;
	
	int i = 0;
    while (pEnumerator)
    {
        HRESULT hr = pEnumerator->Next(WBEM_INFINITE, 1, 
            &pclsObj, &uReturn);
        if(0 == uReturn)
        {
            break;
        }
		
        VARIANT vtProp;
        //获取信息  硬盘序列号
        hr = pclsObj->Get(L"SerialNumber", 0, &vtProp, 0, 0);
		wcout << "Serial Number : " << i << vtProp.bstrVal << endl;
		
		/*********
		hr = pclsObj->Get(L"Name", 0, &vtProp, 0, 0);
		wcout << "name "<<vtProp.bstrVal << endl;
		hr = pclsObj->Get(L"Manufacturer", 0, &vtProp, 0, 0);
		wcout << vtProp.bstrVal << endl;
		hr = pclsObj->Get(L"Version", 0, &vtProp, 0, 0);
		wcout << vtProp.bstrVal << endl;
		hr = pclsObj->Get(L"CurrentLanguage", 0, &vtProp, 0, 0);
		wcout << vtProp.bstrVal << endl;
		//*******/
		
		
        VariantClear(&vtProp);


			 unsigned int fn_id = 0;
	if(argc == 2){
		fn_id = atoi(argv[1]);
	}
    unsigned int cpu[4];
    cpuInfo(cpu,fn_id);
    printf("%08X-%08X-%08X-%08X\n",cpu[0],cpu[1],cpu[2],cpu[3]);

    }
	

	
    // Cleanup
    // ========
    //CGetSystemInfo::GetCPUID()  ;
	
    pSvc->Release();
    pLoc->Release();
    pEnumerator->Release();
    pclsObj->Release();
    CoUninitialize();
	
	
	//cpuidinfo

    return 0;   // Program successfully completed.
}