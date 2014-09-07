#ifndef __PAMTEST_H
#define __PAMTEST_H

#include "pam.h"

/* Let's edit comments. */

/* PAM Conversation -- pam_conv is from _pam_types.h */
static struct pam_conv misc_conv_struct =
{ 
  misc_conv, /* from pam_misc.h */
  NULL       /* data to be passed back to this app. */
};

/* for testing */
int test_conv(int num_msg, const struct pam_message **msg,
	 struct pam_response **resp, void *appdata_ptr);

static struct pam_conv test_conv_struct =
{
  test_conv,
  NULL
};

/* print pam items, for debugging */
void print_pam_items(pam_handle_t* pamh);

#endif /* ifndef __PAMTEST_H */
