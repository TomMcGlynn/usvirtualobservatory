#include <stdio.h>

#include "pam_cmd.h"

/* A simple command-line PAM verifier.
 * Written by Bill Baker, pam_cmd@orangecrayon.com, in 2008.
 * In the public domain ftw! */
int main(int argc, char* argv[]) {
  char* cmd_name = "pam_cmd";
  if (argc >= 1 && argv[0] != NULL) cmd_name = argv[0];
  if (argc < 3 || argc > 4) {
    usage(cmd_name);
    return 1;
  }
  int i = 1;
  int account_only = 0;
  if (strcmp("-a", argv[i]) == 0 || strcmp("--account", argv[i]) == 0) {
    ++i;
    account_only = 1;
  }
  char* pam_module_name = argv[i++];
  callback_data.username = argv[i++];
  /*
  printf("account? %d, module %s, username %s, argc %d, i %d\n",
	 account_only, pam_module_name, callback_data.username, argc, i);
  */

  if (argc > i)
    callback_data.password = argv[i++];
  else if (!account_only) { /* no more args, so read password from stdin */
    /* scanf("%as", &callback_data.password); */ /* causes bus error */
    callback_data.password = malloc(4097);
    int count = scanf("%4096s", callback_data.password);
    /* printf("Read password from stdin: \"%s\" (%d)\n", callback_data.password, count); */
    if (count != 1) {
      printf("Error: Did not receive password on stdin.\n");
      usage(cmd_name);
    }
  }

  pam_handle_t* pamh;

  int start = pam_start(pam_module_name, NULL, &cmd_conv_struct, &pamh);
  int auth = pam_authenticate(pamh, 0);
  int end = pam_end(pamh, start);

  if (auth == PAM_SUCCESS)
    printf("Authentication for %s successful.\n", callback_data.username);
  else
    printf("%s\n", pam_strerror(pamh, auth));
  return auth;
}

int cmd_conv(int num_msg, const struct pam_message **msg,
	 struct pam_response **resp, void *appdata_ptr)
{
    struct pam_callback_data* callback_data = appdata_ptr;
    
    /* struct pam_response* resp_array = calloc(num_msg, sizeof(struct pam_response)); */
    struct pam_response* resp_array = malloc(sizeof(struct pam_response) * num_msg);
    if (resp_array == NULL) {
      printf("Unable to allocate response.\n");
      return PAM_CONV_ERR;
    }

    int i;
    for (i = 0; i < num_msg; ++i) {
	const struct pam_message* message = msg[i];
	resp_array[i].resp_retcode = 0;
	resp_array[i].resp = NULL;
	/* printf("Message: \"%s\" (%d)\n", message->msg, message->msg_style); */
	if (message->msg_style == PAM_PROMPT_ECHO_ON) { /** Probably username request */
	  char* answer = malloc(strlen(callback_data->username));
	  strcpy(answer, callback_data->username);
	  resp_array[i].resp = answer;
	}
	else if (message->msg_style == PAM_PROMPT_ECHO_OFF) { /** Probably password request */
	  char* answer = malloc(strlen(callback_data->password));
	  strcpy(answer, callback_data->password);
	  resp_array[i].resp = answer;
	}
	else if (message->msg_style == PAM_ERROR_MSG || message->msg_style == PAM_TEXT_INFO)
	  printf("%s\n", message->msg);
	else {
	  printf("Unknown PAM message style: %d (%s).\n", message->msg_style, message->msg);
	  /* we should free resp_array, but we won't because we're going to exit. */
	  return PAM_CONV_ERR;
	}
    }

    *resp = resp_array;
    return PAM_SUCCESS;
}

void usage(char* name) {
  printf("usage: %s [options] <pam_module> <username> [<pwd> | < <pwd on stdin>]\n", name);
  printf("Options:\n");
  printf("  --account | -a   only check existence of account; don't attempt authentication\n");
}
