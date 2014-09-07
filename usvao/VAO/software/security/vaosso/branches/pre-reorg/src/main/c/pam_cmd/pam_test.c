#include <stdio.h>

#include "pam_test.h"

/* A simple PAM client to debug PAM configurations. */
int main(int argc, char* argv[]) 
{
  char* pam_name = "pam_test";
  if (argc >= 2 && argv[1] != NULL) {
    pam_name = argv[1];
  }
  printf("Using service name \"%s\".\n", pam_name);

  pam_handle_t* pamh;

  int start = pam_start(pam_name, NULL, &test_conv_struct, &pamh);
  /* int start = pam_start("pam_test", NULL, &misc_conv_struct, &pamh); */
  printf("  Started: %s (%d)\n", pam_strerror(pamh, start), start);
  printf("  pam_handle = %d\n", pamh);
  print_pam_items(pamh);

  int auth = pam_authenticate(pamh, 0);
  printf("Authenticated: %s (%d)\n", pam_strerror(pamh, auth), auth);
  print_pam_items(pamh);

  int end = pam_end(pamh, start);
  printf("Ended: %s (%d)\n", pam_strerror(pamh, end), end);
  print_pam_items(pamh);
}

int test_conv(int num_msg, const struct pam_message **msg,
	 struct pam_response **resp, void *appdata_ptr)
{
    /* printf("Num msgs: %d\n", num_msg); */
    int i;
    for (i = 0; i < num_msg; ++i) {
	const struct pam_message* message = msg[i];
	printf("Message# %d: Text = \"%s\"; Style = %d\n", i, message->msg, message->msg_style);
    }

    int result = misc_conv(num_msg, msg, resp, appdata_ptr);

    for (i = 0; i < num_msg; ++i) {
	const struct pam_response* response = resp[i];
	printf("Response# %d: Text = \"%s\"; Code = %d\n", i, response->resp, response->resp_retcode);
    }

    printf("Returned %d\n", result);
    return result;
}

char* _describe_item_type(int item_type, char* buf, int len) {
    switch (item_type) {
    case PAM_SERVICE : return "service";
    case PAM_USER : return "user";
    case PAM_TTY : return "tty";
    case PAM_RHOST : return "rhost";
    case PAM_CONV : return "conversation";
    case PAM_RUSER : return "remote user";
    case PAM_USER_PROMPT : return "user prompt";
    default: {
	int succeeded = snprintf(buf, len, "unknown (%d)", item_type);
	if (succeeded < 0) return "not enough room";
	else return buf;
    }
    }
}

void _describe_item(int item_type, void const* item) {
    /* PAM_CONV is a struct */
    if (item_type == PAM_CONV) printf("    [PAM conversation struct]\n");
    /* Everything else is a string */
    else {
	if (item == NULL) item = "[NULL]";
	char buf[50];
	printf("    %s = %s\n", _describe_item_type(item_type, buf, 50), (char*) item);
    }
}

/* Helper to print_pam_items */
void _print_pam_item_1(const pam_handle_t* pamh, int item_type) {
    const void *item;
    int ret = pam_get_item(pamh, item_type, &item);
    if (ret == PAM_SUCCESS)
	_describe_item(item_type, item);
#ifndef __DARWIN_UNIX03 /* Mac defines this differently */
    else if (ret == PAM_BAD_ITEM);
#endif
    else printf("[Unexpected pam_get_item() return value: %d]\n", ret);
}

/* Debugging: print the current PAM environment. */
void print_pam_items(pam_handle_t* pamh) {
    _print_pam_item_1(pamh, PAM_SERVICE);
    _print_pam_item_1(pamh, PAM_USER);
    _print_pam_item_1(pamh, PAM_TTY);
    _print_pam_item_1(pamh, PAM_RHOST);
    _print_pam_item_1(pamh, PAM_CONV);
    _print_pam_item_1(pamh, PAM_RUSER);
    _print_pam_item_1(pamh, PAM_USER_PROMPT);
}
