#define SUCCESS					0
#define FAILURE					1

#define MAXPATHLEN				MAX_PATH

#define ELEVATE_LOCK_FILE		"elevate.lock"
#define RUNNING_LOCK_FILE		"running.lock"
#define F_OK					00

#define ERRORBOX(message)		MessageBox(NULL, _T(message), \
										L"UAC Demo", (MB_ICONERROR | MB_SERVICE_NOTIFICATION));
#define SUCCESSBOX(message)		MessageBox(NULL, _T(message), \
										L"UAC Demo", (MB_OK | MB_ICONINFORMATION | MB_SERVICE_NOTIFICATION));

// Closes the handle if valid and if the demo app is elevated returns with the
// return code specified.
#define EXIT_IF_ELEVATED(path, handle, retCode) \
	{ \
	  if (handle != INVALID_HANDLE_VALUE) { \
		CloseHandle(handle); \
	  } \
	  if (_waccess(path, F_OK) == 0 && _wremove(path) != 0) { \
		return retCode; \
	  } \
	}

LPWSTR BuildCommandLine(int argc, LPWSTR *argv);
LPWSTR ArgToString(LPWSTR d, const LPWSTR s);
int ArgStrLen(const LPWSTR s);