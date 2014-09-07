#ifndef __PAMCMD_H
#define __PAMCMD_H

#include "pam.h"

/* Struct to be passed back to this app. */
struct pam_callback_data {
  char* username;
  char* password;
};

static struct pam_callback_data callback_data = {
  NULL,
  NULL
};

int cmd_conv(int num_msg, const struct pam_message **msg,
	 struct pam_response **resp, void *appdata_ptr);

/* Defined in _pam_types.h. */
static struct pam_conv cmd_conv_struct = {
  cmd_conv,
  &callback_data
};

void usage(char* name);

#endif /* ifndef __PAMCMD_H */
